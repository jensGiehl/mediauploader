package de.agiehl.mediauploader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Upload upload, Security security) {

    public record Upload(Path directory) {
    }

    public record Security(String password, String cookieSecret, Duration cookieLifetime, boolean secureCookie) {
    }
}
