package com.beanbuddies.BeanBuddies.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * Simple startup validator that ensures required environment configuration is present.
 * Fails fast with a friendly error message when `app.base-url` (APP_BASE_URL) is missing.
 *
 * Behavior:
 * - By default the validator enforces APP_BASE_URL is set.
 * - The requirement can be disabled by setting `app.require-base-url=false` (used by tests).
 * - The validator also skips enforcement when the active profile includes `test`.
 */
@Component
public class StartupValidator {

    @Autowired
    private Environment env;

    @Value("${app.base-url:}")
    private String appBaseUrl;

    // Feature flag: allow tests or special runs to disable the strict requirement
    @Value("${app.require-base-url:true}")
    private boolean requireBaseUrl;

    @PostConstruct
    public void validate() {
        // Skip if tests explicitly disable the requirement
        if (!requireBaseUrl) {
            return;
        }

        // Also skip validation when running under the 'test' profile
        if (env != null && env.acceptsProfiles(Profiles.of("test"))) {
            return;
        }

        // Skip enforcement when running under unit/integration test runtime (JUnit on classpath)
        if (isRunningUnderTestRuntime()) {
            return;
        }

        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            throw new IllegalStateException("Missing required configuration: APP_BASE_URL (app.base-url). Set APP_BASE_URL to your application's public base URL (e.g. https://your-app.onrender.com) before starting.");
        }
    }

    /**
     * Best-effort detection whether we are running in a test runtime by checking for common
     * JUnit classes on the classpath. In normal production/startup these classes are not
     * present, but in unit/integration test runs they are.
     */
    private boolean isRunningUnderTestRuntime() {
        try {
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("org.junit.Test");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }
}
