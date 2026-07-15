package de.agiehl.mediauploader.notification;

import de.agiehl.mediauploader.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UploadReminderTest {

    @TempDir
    Path uploadDirectory;

    private final RecordingMessageSender messageSender = new RecordingMessageSender();

    @Test
    void sendsNumberOfRegularFiles() throws Exception {
        Files.writeString(uploadDirectory.resolve("first.jpg"), "first");
        Files.writeString(uploadDirectory.resolve("second.mp4"), "second");
        Files.createDirectory(uploadDirectory.resolve("subdirectory"));

        reminder(uploadDirectory).checkUploads();

        assertThat(messageSender.messages).containsExactly("Es gibt 2 Dateien auf dem Server.");
    }

    @Test
    void sendsNothingForEmptyDirectory() {
        reminder(uploadDirectory).checkUploads();

        assertThat(messageSender.messages).isEmpty();
    }

    @Test
    void sendsNothingWhenUploadDirectoryDoesNotExist() {
        reminder(uploadDirectory.resolve("missing")).checkUploads();

        assertThat(messageSender.messages).isEmpty();
    }

    private UploadReminder reminder(Path directory) {
        var properties = new AppProperties(
                new AppProperties.Upload(directory),
                new AppProperties.Security("password", "secret", Duration.ofDays(1), false),
                new AppProperties.Telegram(true, "token", "chat", URI.create("https://api.telegram.org"),
                        "0 0 20 * * WED", "Europe/Berlin", "Es gibt {0} Dateien auf dem Server."));
        return new UploadReminder(properties, messageSender);
    }

    private static class RecordingMessageSender implements TelegramMessageSender {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void send(String message) {
            messages.add(message);
        }
    }
}
