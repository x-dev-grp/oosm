package com.xdev.ooms.security.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SessionRefreshResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType = "Bearer";

    private List<String> authorities;

    public SessionRefreshResponse() {
    }

    public SessionRefreshResponse(String accessToken, List<String> authorities) {
        this.accessToken = accessToken;
        this.authorities = authorities;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }
}
