package com.xdev.ooms.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.boot.logging.Log;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Applies restart-required app_setting values to the Spring Environment before context refresh,
 * so properties such as {@code SPRINGDOC_ENABLED} match the database after a backend restart.
 */
public class AppSettingsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "appSettingsDatabase";

    private static final int JDBC_RETRY_ATTEMPTS = 3;
    private static final long JDBC_RETRY_DELAY_MS = 500;

    /**
     * setting_key in app_setting -> Spring property / env var name used in application.yml.
     */
    private static final Map<String, String> BOOTSTRAP_KEYS = Map.of(
            "SPRINGDOC_ENABLED", "SPRINGDOC_ENABLED",
            "SEARCH_DEBUG_ON_STARTUP", "SEARCH_DEBUG_ON_STARTUP",
            "HEALTH_SHOW_DETAILS", "HEALTH_SHOW_DETAILS",
            "NEW_RELIC_APM_ENABLED", "NEW_RELIC_APM_ENABLED",
            "NEW_RELIC_APP_NAME", "NEW_RELIC_APP_NAME",
            "NEW_RELIC_LOG_FORWARDING_ENABLED", "NEW_RELIC_LOG_FORWARDING_ENABLED",
            "NEW_RELIC_REGION", "NEW_RELIC_REGION"
    );

    /**
     * Additional Spring properties to set from a bootstrap key so {@code application.yml}
     * placeholders and {@code @ConditionalOnProperty} see the database value even when an
     * OS env var (e.g. Render's SPRINGDOC_ENABLED=false) would otherwise win.
     */
    private static final Map<String, List<String>> DERIVED_PROPERTIES = Map.of(
            "SPRINGDOC_ENABLED", List.of("springdoc.api-docs.enabled"),
            "SEARCH_DEBUG_ON_STARTUP", List.of("app.debug.search-on-startup"),
            "HEALTH_SHOW_DETAILS", List.of("management.endpoint.health.show-details"),
            "NEW_RELIC_LOG_FORWARDING_ENABLED", List.of("NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED")
    );

    private static final Set<String> BOOLEAN_BOOTSTRAP_KEYS = Set.of(
            "SPRINGDOC_ENABLED",
            "SEARCH_DEBUG_ON_STARTUP",
            "NEW_RELIC_APM_ENABLED",
            "NEW_RELIC_LOG_FORWARDING_ENABLED"
    );

    private final Log log;

    public AppSettingsEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(getClass());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String jdbcUrl = resolveJdbcUrl(environment);
        String username = resolveUsername(environment);
        String password = resolvePassword(environment);

        if (!isResolvableJdbcUrl(jdbcUrl)) {
            log.warn("Skipping app_setting bootstrap: JDBC URL is missing or still contains unresolved placeholders");
            return;
        }

        log.info("Loading restart-required app_setting values from database (url={})", maskJdbcUrl(jdbcUrl));

        Map<String, Object> properties = loadBootstrapProperties(jdbcUrl, username, password);
        if (properties.isEmpty()) {
            log.warn(
                    "app_setting bootstrap found no values for keys {} — "
                            + "springdoc.api-docs.enabled and other restart-required settings will use application.yml defaults",
                    BOOTSTRAP_KEYS.keySet()
            );
            return;
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));

        String springdocRuntime = environment.getProperty("springdoc.api-docs.enabled", "false");
        String newRelicApm = environment.getProperty("NEW_RELIC_APM_ENABLED", "false");
        log.info(
                "app_setting bootstrap succeeded: loaded {} propert(ies) {} (springdoc.api-docs.enabled={}, NEW_RELIC_APM_ENABLED={})",
                properties.size(),
                properties.keySet(),
                springdocRuntime,
                newRelicApm
        );
    }

    @Override
    public int getOrder() {
        // Run last so addFirst() wins over config-data, system env, and any other post-processors.
        return Ordered.LOWEST_PRECEDENCE;
    }

    private Map<String, Object> loadBootstrapProperties(String jdbcUrl, String username, String password) {
        Map<String, Object> properties = new LinkedHashMap<>();
        String placeholders = BOOTSTRAP_KEYS.keySet().stream().map(key -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT setting_key, value FROM app_setting"
                + " WHERE setting_key IN (" + placeholders + ")"
                + " AND value IS NOT NULL AND trim(value) <> ''";

        SQLException lastFailure = null;
        for (int attempt = 1; attempt <= JDBC_RETRY_ATTEMPTS; attempt++) {
            try {
                ensurePostgresDriver();
                try (Connection connection = openConnection(jdbcUrl, username, password);
                     PreparedStatement statement = connection.prepareStatement(sql)) {
                    int index = 1;
                    for (String key : BOOTSTRAP_KEYS.keySet()) {
                        statement.setString(index++, key);
                    }
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            String settingKey = resultSet.getString("setting_key");
                            String value = resultSet.getString("value");
                            String propertyName = BOOTSTRAP_KEYS.get(settingKey);
                            if (propertyName != null && StringUtils.hasText(value)) {
                                String normalized = normalizeBootstrapValue(settingKey, value);
                                properties.put(propertyName, normalized);
                                applyDerivedProperties(properties, settingKey, normalized);
                            }
                        }
                    }
                }
                return properties;
            } catch (SQLException ex) {
                lastFailure = ex;
                if (attempt < JDBC_RETRY_ATTEMPTS) {
                    log.warn(
                            "app_setting bootstrap JDBC attempt {}/{} failed ({}): {}",
                            attempt,
                            JDBC_RETRY_ATTEMPTS,
                            ex.getSQLState(),
                            ex.getMessage()
                    );
                    sleep();
                }
            } catch (ClassNotFoundException ex) {
                log.error("app_setting bootstrap failed: PostgreSQL driver not on classpath — {}", ex.getMessage());
                return properties;
            } catch (Exception ex) {
                log.error("app_setting bootstrap failed unexpectedly: {}", ex.toString());
                return properties;
            }
        }

        if (lastFailure != null) {
            log.error(
                    "Failed to load app_setting bootstrap properties after {} attempts (url={}): [{}] {}",
                    JDBC_RETRY_ATTEMPTS,
                    maskJdbcUrl(jdbcUrl),
                    lastFailure.getSQLState(),
                    lastFailure.getMessage()
            );
        }
        return properties;
    }

    private static Connection openConnection(String jdbcUrl, String username, String password) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        try (Statement init = connection.createStatement()) {
            init.execute("SET search_path TO public");
        }
        return connection;
    }

    private static void applyDerivedProperties(Map<String, Object> properties, String settingKey, String value) {
        List<String> derived = DERIVED_PROPERTIES.get(settingKey);
        if (derived == null) {
            return;
        }
        for (String propertyName : derived) {
            properties.put(propertyName, value);
        }
    }

    private static String normalizeBootstrapValue(String settingKey, String value) {
        String trimmed = value.trim();
        if (BOOLEAN_BOOTSTRAP_KEYS.contains(settingKey)) {
            if ("true".equalsIgnoreCase(trimmed)) {
                return "true";
            }
            if ("false".equalsIgnoreCase(trimmed)) {
                return "false";
            }
        }
        return trimmed;
    }

    private static void ensurePostgresDriver() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = AppSettingsEnvironmentPostProcessor.class.getClassLoader();
        }
        Class.forName("org.postgresql.Driver", true, classLoader);
    }

    private static String resolveJdbcUrl(ConfigurableEnvironment environment) {
        String dbUrl = firstNonBlank(
                environment.getProperty("DB_URL"),
                environment.getProperty("spring.datasource.url")
        );
        if (isResolvableJdbcUrl(dbUrl)) {
            return dbUrl;
        }

        String host = firstNonBlank(environment.getProperty("PGHOST"), "localhost");
        String port = firstNonBlank(environment.getProperty("PGPORT"), "5432");
        String database = firstNonBlank(environment.getProperty("PGDATABASE"), "osm");
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    private static String resolveUsername(ConfigurableEnvironment environment) {
        String username = firstNonBlank(
                environment.getProperty("DB_USER"),
                environment.getProperty("PGUSER"),
                environment.getProperty("spring.datasource.username")
        );
        return StringUtils.hasText(username) ? username : "postgres";
    }

    private static String resolvePassword(ConfigurableEnvironment environment) {
        String password = firstNonBlank(
                environment.getProperty("DB_PASS"),
                environment.getProperty("PGPASSWORD"),
                environment.getProperty("spring.datasource.password")
        );
        return password != null ? password : "root";
    }

    private static String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate.trim();
            }
        }
        return null;
    }

    private static boolean isResolvableJdbcUrl(String url) {
        return StringUtils.hasText(url) && !url.contains("${");
    }

    private static String maskJdbcUrl(String jdbcUrl) {
        if (!StringUtils.hasText(jdbcUrl)) {
            return "<empty>";
        }
        int at = jdbcUrl.indexOf('@');
        if (at > 0) {
            return jdbcUrl.substring(0, Math.min(jdbcUrl.indexOf("://") + 3, jdbcUrl.length())) + "***@" + jdbcUrl.substring(at + 1);
        }
        return jdbcUrl;
    }

    private static void sleep() {
        try {
            Thread.sleep(AppSettingsEnvironmentPostProcessor.JDBC_RETRY_DELAY_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
