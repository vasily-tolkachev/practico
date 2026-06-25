package com.myproject.practico.adapter.in.web.telegram;

import com.myproject.practico.adapter.in.web.telegram.dto.TelegramUpdate;
import com.myproject.practico.adapter.in.web.telegram.command.TelegramCommandRouter;
import com.myproject.practico.application.port.in.SendMessageUseCase;
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
    private final SendMessageUseCase sendMessageUseCase;

    @PostMapping("/webhook")
    public void webhook(@RequestBody TelegramUpdate update) {
        if (update == null || update.message() == null) {
            return;
        }

        String userId = update.message().chat().id();
        String response = commandRouter.route(userId, update.message().text());

        sendMessageUseCase.send(userId, response);
    }
}
