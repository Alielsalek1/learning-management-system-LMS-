package com.main.lms.dtos;

import lombok.Data;

@Data
public class GenerateQuizRequestDTO {
    private Long courseId;
    private Integer questionCount;
}
