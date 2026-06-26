package com.xdev.ooms.security.user.service;

import com.xdev.ooms.security.companyprofile.entity.CompanyProfile;
import com.xdev.ooms.security.companyprofile.repository.CompanyProfileRepository;
import com.xdev.ooms.security.user.dto.SessionRefreshResponse;
import com.xdev.ooms.security.user.entity.OOSMUser;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class UserSessionService {

    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final RegisteredClientRepository registeredClientRepository;
    private final UserService userService;
    private final CompanyProfileRepository companyProfileRepository;
    private final AuthorizationServerContext authorizationServerContext;

    public UserSessionService(OAuth2AuthorizationService authorizationService,
                              OAuth2TokenGenerator<?> tokenGenerator,
                              RegisteredClientRepository registeredClientRepository,
                              UserService userService,
                              CompanyProfileRepository companyProfileRepository,
                              @Value("${spring.security.oauth2.resource-server.jwt.jwk-set-uri}") String jwkSetUri) {
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.registeredClientRepository = registeredClientRepository;
        this.userService = userService;
        this.companyProfileRepository = companyProfileRepository;
        String resolvedIssuer = resolveIssuer(jwkSetUri);
        this.authorizationServerContext = new StaticAuthorizationServerContext(resolvedIssuer);
    }

    public SessionRefreshResponse refreshSession(String accessTokenValue) {
        long startTime = System.currentTimeMillis();
        OOSMLogger.logMethodEntry(this.getClass(), "refreshSession", "Refreshing session permissions");

        OAuth2Authorization authorization = authorizationService.findByToken(
                accessTokenValue, OAuth2TokenType.ACCESS_TOKEN);
        if (authorization == null) {
            OOSMLogger.logSecurityEvent(this.getClass(), "SESSION_REFRESH_NOT_FOUND",
                    "No authorization found for access token");
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }

        String username = authorization.getPrincipalName();
        OOSMUser user = userService.getByUsernameWithFreshPermissions(username);
        if (user == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }
        validateActiveUser(user);

        RegisteredClient client = registeredClientRepository.findById(authorization.getRegisteredClientId());
        if (client == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        UsernamePasswordAuthenticationToken freshPrincipal =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        Set<String> scopes = Collections.emptySet();
        AuthorizationServerContextHolder.setContext(authorizationServerContext);
        try {
            OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                    .authorization(authorization)
                    .principal(freshPrincipal)
                    .registeredClient(client)
                    .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                    .authorizedScopes(scopes)
                    .authorizationServerContext(authorizationServerContext)
                    .authorizationGrantType(authorization.getAuthorizationGrantType())
                    .authorizationGrant(freshPrincipal)
                    .build();

            OAuth2Token generatedAccessToken = tokenGenerator.generate(tokenContext);
            if (generatedAccessToken == null) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
            }

            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    generatedAccessToken.getTokenValue(),
                    generatedAccessToken.getIssuedAt(),
                    generatedAccessToken.getExpiresAt(),
                    tokenContext.getAuthorizedScopes());

            OAuth2Authorization.Builder saveBuilder = OAuth2Authorization.from(authorization);
            if (generatedAccessToken instanceof ClaimAccessor) {
                saveBuilder.token(accessToken,
                        metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                                ((ClaimAccessor) generatedAccessToken).getClaims()));
            } else {
                saveBuilder.accessToken(accessToken);
            }

            authorizationService.save(saveBuilder.build());

            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            OOSMLogger.logMethodExit(this.getClass(), "refreshSession",
                    "Session refreshed for user: " + username + ", authorities: " + authorities.size());
            OOSMLogger.logPerformance(this.getClass(), "refreshSession", startTime, System.currentTimeMillis());

            return new SessionRefreshResponse(accessToken.getTokenValue(), authorities);
        } finally {
            AuthorizationServerContextHolder.resetContext();
        }
    }

    private void validateActiveUser(OOSMUser user) {
        if (user.isLocked()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }
        if (user.getRole() == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }
        if (!user.getRole().getRoleName().equalsIgnoreCase("OOSMADMIN")) {
            CompanyProfile companyProfile = companyProfileRepository.findById(user.getTenantId()).orElse(null);
            if (companyProfile == null || !companyProfile.isActive()) {
                throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
            }
        }
    }

    private static String resolveIssuer(String jwkSetUri) {
        if (jwkSetUri != null && jwkSetUri.endsWith("/oauth2/jwks")) {
            return jwkSetUri.substring(0, jwkSetUri.length() - "/oauth2/jwks".length());
        }
        return "http://localhost:8084";
    }

    private static final class StaticAuthorizationServerContext implements AuthorizationServerContext {
        private final AuthorizationServerSettings settings;

        private StaticAuthorizationServerContext(String issuer) {
            this.settings = AuthorizationServerSettings.builder().issuer(issuer).build();
        }

        @Override
        public String getIssuer() {
            return settings.getIssuer();
        }

        @Override
        public AuthorizationServerSettings getAuthorizationServerSettings() {
            return settings;
        }
    }
}
