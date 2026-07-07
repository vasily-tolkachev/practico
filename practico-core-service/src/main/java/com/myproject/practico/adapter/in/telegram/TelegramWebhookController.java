package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.adapter.in.telegram.dto.TelegramUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
public class TelegramWebhookController {

    private final TelegramIncomingMessageHandler incomingMessageHandler;

    @PostMapping("/webhook")
    public void webhook(@RequestBody TelegramUpdate update) {
        if (update == null || update.message() == null) {
            return;
        }

        incomingMessageHandler.handle(
                update.message().chat().id(),
                update.message().text()
        );
    }
}
