package com.main.lms.dtos;

import com.main.lms.enums.QuestionType;
import lombok.Data;

@Data
public class QuestionResponseDTO {
    private Long questionId;
    private String content;
    private QuestionType type;
    private String courseTitle;
    private String answer;
}
