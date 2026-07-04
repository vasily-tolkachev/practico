package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.adapter.in.telegram.dto.TelegramUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
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
