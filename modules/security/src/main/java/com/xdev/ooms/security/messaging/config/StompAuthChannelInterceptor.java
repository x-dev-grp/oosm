package com.xdev.ooms.security.messaging.config;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    static final String SESSION_ACCESS_TOKEN = "chat.access_token";
    static final String SESSION_USERNAME = "chat.username";

    private final JwtDecoder jwtDecoder;

    public StompAuthChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        Authentication authentication = resolveAuthentication(accessor);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                accessor.setUser(authentication);
            }
            return message;
        }

        if (requiresAuthentication(accessor.getCommand())) {
            throw new AccessDeniedException("Authenticated user required");
        }

        return message;
    }

    @Override
    public void afterSendCompletion(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel,
            boolean sent,
            Exception ex) {
        SecurityContextHolder.clearContext();
    }

    private Authentication resolveAuthentication(StompHeaderAccessor accessor) {
        Authentication existing = authenticationFromSessionUser(accessor);
        if (existing != null) {
            return existing;
        }

        String token = resolveToken(accessor);
        if (token == null || token.isBlank()) {
            token = sessionToken(accessor);
        }
        if (token != null && !token.isBlank()) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                storeSessionContext(accessor, token, extractUsername(jwt));
                return authentication;
            } catch (JwtException ex) {
                Authentication fallback = authenticationFromStoredUsername(accessor);
                if (fallback != null) {
                    return fallback;
                }
            }
        }

        return authenticationFromStoredUsername(accessor);
    }

    private Authentication authenticationFromSessionUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
            return jwtAuth;
        }
        if (accessor.getUser() instanceof Authentication auth && auth.isAuthenticated()) {
            return auth;
        }
        return null;
    }

    private Authentication authenticationFromStoredUsername(StompHeaderAccessor accessor) {
        String username = sessionUsername(accessor);
        if (username == null || username.isBlank()) {
            return null;
        }
        return new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.NO_AUTHORITIES);
    }

    private void storeSessionContext(StompHeaderAccessor accessor, String token, String username) {
        if (accessor.getSessionAttributes() == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return;
        }
        accessor.getSessionAttributes().put(SESSION_ACCESS_TOKEN, token);
        if (username != null && !username.isBlank()) {
            accessor.getSessionAttributes().put(SESSION_USERNAME, username);
        }
    }

    private String sessionToken(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            return null;
        }
        Object token = accessor.getSessionAttributes().get(SESSION_ACCESS_TOKEN);
        return token != null ? token.toString() : null;
    }

    private String sessionUsername(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            return null;
        }
        Object username = accessor.getSessionAttributes().get(SESSION_USERNAME);
        return username != null ? username.toString() : null;
    }

    private String extractUsername(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            return subject;
        }
        Object osmUserClaim = jwt.getClaim("osmUser");
        if (osmUserClaim instanceof Map<?, ?> map && map.get("username") != null) {
            return map.get("username").toString();
        }
        return null;
    }

    private boolean requiresAuthentication(StompCommand command) {
        return StompCommand.SEND.equals(command)
                || StompCommand.SUBSCRIBE.equals(command)
                || StompCommand.UNSUBSCRIBE.equals(command)
                || StompCommand.CONNECT.equals(command);
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        String accessToken = accessor.getFirstNativeHeader("access_token");
        if (accessToken != null && !accessToken.isBlank()) {
            return accessToken;
        }
        return null;
    }
}
