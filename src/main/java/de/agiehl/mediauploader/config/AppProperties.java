package de.agiehl.mediauploader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Upload upload, Security security, Telegram telegram) {

    public record Upload(Path directory) {
    }

    public record Security(String password, String cookieSecret, Duration cookieLifetime, boolean secureCookie) {
    }

    public record Telegram(boolean enabled, String botToken, String chatId, URI apiBaseUrl,
                           String cron, String zone, String messageTemplate) {
    }
}
