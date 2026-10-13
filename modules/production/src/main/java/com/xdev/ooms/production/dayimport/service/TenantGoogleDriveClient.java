package com.xdev.ooms.production.dayimport.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.ooms.sharedkernel.config.TenantContext;
import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Per-tenant Google Drive client using the refresh token from OAuth connect.
 */
@Primary
@Component
public class TenantGoogleDriveClient implements DayImportDriveClient {

    private final GoogleDriveOAuthService oauthService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiBase;

    @org.springframework.beans.factory.annotation.Autowired
    public TenantGoogleDriveClient(GoogleDriveOAuthService oauthService, ObjectMapper objectMapper) {
        this(oauthService, objectMapper, HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(15)).build(), "https://www.googleapis.com/drive/v3");
    }

    TenantGoogleDriveClient(GoogleDriveOAuthService oauthService, ObjectMapper objectMapper, HttpClient httpClient, String apiBase) {
        this.oauthService = oauthService;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.apiBase = apiBase;
    }

    @Override
    public boolean isAvailable() {
        return oauthService.isOAuthConfigured()
                && oauthService.isConnected(TenantContext.getCurrentTenant());
    }

    @Override
    public List<DriveFileRef> listXlsx(String folderId) throws Exception {
        OOSMLogger.logMethodEntry(getClass(), "listXlsx", folderId, TenantContext.getCurrentTenant());
        String accessToken = oauthService.getAccessTokenForCurrentTenant();
        String q = "'" + folderId + "' in parents and trashed=false and ("
                + "mimeType='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'"
                + " or name contains '.xlsx')";
        if (!folderId.matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("Invalid Drive folder ID");
        String url = apiBase + "/files?pageSize=100&fields=nextPageToken,files(id,name)"
                + "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        List<DriveFileRef> out = new ArrayList<>();
        String token = "";
        java.util.Set<String> seen = new java.util.HashSet<>();
        do {
            JsonNode root = getJson(url + (token.isBlank() ? "" : "&pageToken=" + URLEncoder.encode(token, StandardCharsets.UTF_8)), accessToken);
            for (JsonNode f : root.path("files")) {
                String name=f.path("name").asText("");
                if (name.toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx")) out.add(new DriveFileRef(f.path("id").asText(), name));
            }
            token=root.path("nextPageToken").asText("");
            if (!token.isBlank() && !seen.add(token)) throw new IllegalStateException("Repeated Drive page token");
        } while (!token.isBlank());
        OOSMLogger.info(getClass(), "[listXlsx] folder={} count={}", folderId, out.size());
        return out;
    }

    @Override
    public byte[] download(String fileId) throws Exception {
        OOSMLogger.logMethodEntry(getClass(), "download", fileId);
        String accessToken = oauthService.getAccessTokenForCurrentTenant();
        HttpRequest request = HttpRequest.newBuilder().timeout(java.time.Duration.ofSeconds(45))
                .uri(URI.create(apiBase + "/files/" + fileId + "?alt=media"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 300) {
            OOSMLogger.warn(getClass(), "[download] failed fileId={} http={}", fileId, response.statusCode());
            throw new IllegalStateException("Drive download failed: HTTP " + response.statusCode());
        }
        OOSMLogger.info(getClass(), "[download] fileId={} bytes={}", fileId, response.body().length);
        return response.body();
    }

    @Override
    public void moveToFolder(String fileId, String targetFolderId) throws Exception {
        OOSMLogger.logMethodEntry(getClass(), "moveToFolder", fileId, targetFolderId);
        String accessToken = oauthService.getAccessTokenForCurrentTenant();
        JsonNode meta = getJson(
                apiBase + "/files/" + fileId + "?fields=parents",
                accessToken);
        String removeParents = "";
        JsonNode parents = meta.path("parents");
        if (parents.isArray() && !parents.isEmpty()) {
            List<String> ids = new ArrayList<>();
            parents.forEach(p -> ids.add(p.asText()));
            removeParents = String.join(",", ids);
        }
        String url = apiBase + "/files/" + fileId
                + "?addParents=" + URLEncoder.encode(targetFolderId, StandardCharsets.UTF_8);
        if (!removeParents.isBlank()) {
            url += "&removeParents=" + URLEncoder.encode(removeParents, StandardCharsets.UTF_8);
        }
        HttpRequest request = HttpRequest.newBuilder().timeout(java.time.Duration.ofSeconds(45))
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Drive move failed: HTTP " + response.statusCode() + " " + response.body());
        }
    }

    private JsonNode getJson(String url, String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().timeout(java.time.Duration.ofSeconds(45))
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Drive API failed: HTTP " + response.statusCode() + " " + response.body());
        }
        return objectMapper.readTree(response.body());
    }
}
