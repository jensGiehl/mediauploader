package de.agiehl.mediauploader.notification;

import de.agiehl.mediauploader.config.AppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true")
class TelegramClient implements TelegramMessageSender {

    private final RestClient restClient;
    private final URI sendMessageEndpoint;
    private final String chatId;

    TelegramClient(RestClient.Builder restClientBuilder, AppProperties properties) {
        AppProperties.Telegram telegram = properties.telegram();
        Assert.hasText(telegram.botToken(), "app.telegram.bot-token must be set when Telegram is enabled");
        Assert.hasText(telegram.chatId(), "app.telegram.chat-id must be set when Telegram is enabled");
        Assert.isTrue(telegram.botToken().matches("[0-9]+:[A-Za-z0-9_-]+"),
                "app.telegram.bot-token has an invalid format");

        String apiBaseUrl = telegram.apiBaseUrl().toString().replaceFirst("/+$", "");
        this.restClient = restClientBuilder.build();
        this.sendMessageEndpoint = URI.create(apiBaseUrl + "/bot" + telegram.botToken() + "/sendMessage");
        this.chatId = telegram.chatId();
    }

    @Override
    public void send(String message) {
        restClient.post()
                .uri(sendMessageEndpoint)
                .body(Map.of("chat_id", chatId, "text", message))
                .retrieve()
                .toBodilessEntity();
    }
}
