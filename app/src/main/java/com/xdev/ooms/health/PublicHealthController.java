package com.xdev.ooms.health;

import com.xdev.ooms.sharedkernel.settings.service.AppSettingsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/health")
public class PublicHealthController {

    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private static final String STATUS_DEGRADED = "DEGRADED";

    private final JdbcTemplate jdbcTemplate;
    private final AppSettingsService appSettingsService;
    private final HttpClient httpClient;

    public PublicHealthController(JdbcTemplate jdbcTemplate, AppSettingsService appSettingsService) {
        this.jdbcTemplate = jdbcTemplate;
        this.appSettingsService = appSettingsService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("backend", backendCheck());
        checks.put("database", databaseCheck());
        checks.put("frontend", frontendCheck());

        boolean allUp = checks.values().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .allMatch(check -> STATUS_UP.equals(check.get("status")));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", allUp ? STATUS_UP : STATUS_DEGRADED);
        body.put("timestamp", Instant.now().toString());
        body.put("checks", checks);

        return ResponseEntity.status(allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    private Map<String, Object> backendCheck() {
        Map<String, Object> check = new LinkedHashMap<>();
        check.put("status", STATUS_UP);
        return check;
    }

    private Map<String, Object> databaseCheck() {
        long startedAt = System.nanoTime();
        Map<String, Object> check = new LinkedHashMap<>();
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            check.put("status", Integer.valueOf(1).equals(result) ? STATUS_UP : STATUS_DOWN);
        } catch (Exception ex) {
            check.put("status", STATUS_DOWN);
            check.put("error", ex.getClass().getSimpleName());
        }
        check.put("latencyMs", elapsedMillis(startedAt));
        return check;
    }

    private Map<String, Object> frontendCheck() {
        long startedAt = System.nanoTime();
        Map<String, Object> check = new LinkedHashMap<>();
        String frontendBaseUrl = appSettingsService.getString("FRONTEND_ENTRY_POINT", "");
        check.put("url", frontendBaseUrl);

        if (frontendBaseUrl == null || frontendBaseUrl.isBlank()) {
            check.put("status", STATUS_DOWN);
            check.put("error", "FrontendUrlNotConfigured");
            check.put("latencyMs", elapsedMillis(startedAt));
            return check;
        }

        try {
            HttpResponse<Void> response = sendFrontendProbe("HEAD", frontendBaseUrl);
            if (response.statusCode() == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                response = sendFrontendProbe("GET", frontendBaseUrl);
            }
            check.put("status", response.statusCode() >= 200 && response.statusCode() < 500 ? STATUS_UP : STATUS_DOWN);
            check.put("httpStatus", response.statusCode());
        } catch (Exception ex) {
            check.put("status", STATUS_DOWN);
            check.put("error", ex.getClass().getSimpleName());
        }
        check.put("latencyMs", elapsedMillis(startedAt));
        return check;
    }

    private HttpResponse<Void> sendFrontendProbe(String method, String frontendBaseUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(frontendBaseUrl))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(5))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
