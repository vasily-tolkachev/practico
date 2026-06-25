package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.adapter.in.telegram.dto.TelegramUpdate;
import com.myproject.practico.application.port.out.MessengerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
public class TelegramWebhookController {

    private final TelegramCommandRouter commandRouter;
    private final MessengerPort messengerPort;

    @PostMapping("/webhook")
    public void webhook(@RequestBody TelegramUpdate update) {
        if (update == null || update.message() == null) {
            return;
        }

        String userId = update.message().chat().id();
        String response = commandRouter.route(userId, update.message().text());

        if (response == null || response.isBlank()) {
            return;
        }

        messengerPort.sendMessage(userId, response);
    }
}
