package com.xdev.ooms.security.securityConfig;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.Collections;

final class PublicClientAuthenticationConverter implements AuthenticationConverter {
    private final BasicAuthenticationConverter basicAuthenticationConverter =
            new BasicAuthenticationConverter();

    @Override
    public Authentication convert(HttpServletRequest request) {
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        boolean supportedGrant = "TOKEN".equalsIgnoreCase(grantType)
                || AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType);
        if (!supportedGrant) {
            return null;
        }

        String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        if (!StringUtils.hasText(clientId)) {
            Authentication basicAuthentication = basicAuthenticationConverter.convert(request);
            clientId = basicAuthentication != null ? basicAuthentication.getName() : null;
        }
        if (!StringUtils.hasText(clientId)) {
            return null;
        }

        return new OAuth2ClientAuthenticationToken(
                clientId,
                ClientAuthenticationMethod.NONE,
                null,
                Collections.emptyMap()
        );
    }
}
