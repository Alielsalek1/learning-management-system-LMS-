package com.main.lms.dtos;

import com.main.lms.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionRequestDTO {
    @NotNull
    private Long courseId;

    @NotBlank
    private String content;

    @NotBlank
    private String answer;

    @NotNull
    private QuestionType type;
}
