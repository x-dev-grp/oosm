package com.xdev.ooms.security.securityConfig;

import com.xdev.ooms.security.user.entity.OSMUser;
import com.xdev.ooms.security.user.service.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OsmJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;
    private final JwtAuthenticationConverter delegate;

    public OsmJwtAuthenticationConverter(UserService userService) {
        this.userService = userService;
        this.delegate = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName("scope");
        delegate.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AbstractAuthenticationToken token = delegate.convert(jwt);
        String username = resolveUsername(jwt, token);
        if (username == null || username.isBlank()) {
            return token;
        }

        OSMUser user = userService.getByUsernameWithFreshPermissions(username);
        if (user == null || user.getRole() == null) {
            return token;
        }

        List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
        String roleName = user.getRole().getRoleName();
        if (roleName != null && !roleName.isBlank()) {
            authorities.add(new SimpleGrantedAuthority(roleName.toUpperCase()));
        }

        return new JwtAuthenticationToken(jwt, authorities, username);
    }

    private static String resolveUsername(Jwt jwt, AbstractAuthenticationToken token) {
        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            return subject;
        }

        Object osmUserClaim = jwt.getClaim("osmUser");
        if (osmUserClaim instanceof Map<?, ?> map && map.get("username") != null) {
            return map.get("username").toString();
        }

        return token.getName();
    }
}
