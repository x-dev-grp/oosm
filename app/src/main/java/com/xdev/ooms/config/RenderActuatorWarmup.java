package com.xdev.ooms.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Warms the actuator health endpoint as soon as Tomcat binds its port so Render's
 * deploy probe does not pay the first-request lazy-init cost on its own timeout window.
 */
@Component
public class RenderActuatorWarmup implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger log = LoggerFactory.getLogger(RenderActuatorWarmup.class);

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        String url = "http://127.0.0.1:" + port + "/actuator/health";
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMinutes(2))
                    .GET()
                    .build();
            long start = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info(
                    "Actuator health warmup {} returned {} in {}ms",
                    url,
                    response.statusCode(),
                    System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.warn("Actuator health warmup failed for {}: {}", url, ex.getMessage());
        }
    }
}
