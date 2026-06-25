package com.myproject.practico.adapter.in.web.question;

import com.myproject.practico.application.port.in.ListQuestionsUseCase;
import com.myproject.practico.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final ListQuestionsUseCase listQuestionsUseCase;

    @GetMapping("/test")
    public List<Question> test() {
        return listQuestionsUseCase.getAll();
    }
}
