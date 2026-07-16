package de.agiehl.mediauploader.notification;

import de.agiehl.mediauploader.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true")
class UploadReminder {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadReminder.class);

    private final Path uploadDirectory;
    private final String messageTemplate;
    private final String scpCommand;
    private final TelegramMessageSender messageSender;

    UploadReminder(AppProperties properties, TelegramMessageSender messageSender) {
        AppProperties.Telegram telegram = properties.telegram();
        Assert.hasText(telegram.scpDirectory(), "app.telegram.scp-directory must be set when Telegram is enabled");
        Assert.hasText(telegram.scpDomain(), "app.telegram.scp-domain must be set when Telegram is enabled");
        this.uploadDirectory = properties.upload().directory().toAbsolutePath().normalize();
        this.messageTemplate = telegram.messageTemplate();
        this.scpCommand = "scp " + telegram.scpDomain() + ":" + telegram.scpDirectory() + "/* .";
        this.messageSender = messageSender;
    }

    @Scheduled(cron = "${app.telegram.cron:0 0 20 * * WED}",
            zone = "${app.telegram.zone:Europe/Berlin}")
    void checkUploads() {
        try {
            long fileCount = countFiles();
            if (fileCount > 0) {
                String reminder = MessageFormat.format(messageTemplate, fileCount);
                messageSender.send(reminder + System.lineSeparator()
                        + "Alle Dateien herunterladen:" + System.lineSeparator() + scpCommand);
                LOGGER.info("Sent Telegram upload reminder for {} files", fileCount);
            }
        } catch (Exception exception) {
            // Do not log the exception message because an HTTP error may contain the bot token in its URL.
            LOGGER.error("Could not send Telegram upload reminder ({})", exception.getClass().getSimpleName());
        }
    }

    private long countFiles() throws IOException {
        if (!Files.isDirectory(uploadDirectory)) {
            return 0;
        }
        try (var files = Files.list(uploadDirectory)) {
            return files.filter(Files::isRegularFile).count();
        }
    }
}
