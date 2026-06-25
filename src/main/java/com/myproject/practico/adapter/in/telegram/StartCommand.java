package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.application.port.in.StartInterviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements TelegramCommand {

    private final StartInterviewUseCase startInterviewUseCase;

    @Override
    public boolean supports(String text) {
        return "/start".equalsIgnoreCase(text);
    }

    @Override
    public String handle(String userId, String text) {
        return startInterviewUseCase.start(userId);
    }
}
