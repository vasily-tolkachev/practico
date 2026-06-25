package com.myproject.practico.api.telegram;

import com.myproject.practico.api.telegram.dto.TelegramUpdate;
import com.myproject.practico.application.port.in.ProcessIncomingMessageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
public class TelegramWebhookController {

    private final ProcessIncomingMessageUseCase useCase;

    @PostMapping("/webhook")
    public void webhook(@RequestBody TelegramUpdate update) {

        if (update == null || update.message() == null) return;

        useCase.process(
                update.message().chat().id(),
                update.message().text()
        );
    }
}