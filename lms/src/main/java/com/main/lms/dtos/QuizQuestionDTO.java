package com.main.lms.dtos;

import com.main.lms.enums.QuestionType;
import lombok.Data;

@Data
public class QuizQuestionDTO {
    private Long questionId;
    private String content;
    private QuestionType type;
}
