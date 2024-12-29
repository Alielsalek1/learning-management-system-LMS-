package com.main.lms.dtos;

import lombok.Data;

@Data
public class GradeDTO {
    private Long studentId;
    private Long quizId;
    private Double grade;
    private int maxGrade;
}
