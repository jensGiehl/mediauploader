package de.agiehl.mediauploader.notification;

import de.agiehl.mediauploader.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TelegramClientTest {

    @Test
    void sendsMessageToConfiguredChat() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://telegram.example/bot123456:ABC/sendMessage"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"chat_id":"-100123","text":"Es gibt 2 Dateien auf dem Server."}
                        """))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        TelegramClient client = new TelegramClient(builder, properties());
        client.send("Es gibt 2 Dateien auf dem Server.");

        server.verify();
    }

    private AppProperties properties() {
        return new AppProperties(
                new AppProperties.Upload(Path.of("Upload")),
                new AppProperties.Security("password", "secret", Duration.ofDays(1), false),
                new AppProperties.Telegram(true, "123456:ABC", "-100123", URI.create("https://telegram.example"),
                        "0 0 20 * * WED", "Europe/Berlin", "Es gibt {0} Dateien auf dem Server."));
    }
}
