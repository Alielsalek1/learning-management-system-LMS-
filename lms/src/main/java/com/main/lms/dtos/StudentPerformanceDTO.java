package com.main.lms.dtos;

import lombok.Data;

@Data
public class StudentPerformanceDTO {
    private Long studentId;
    private String studentName;
    private Double quizAverage;
    private Double assignmentAverage;
    private Double attendancePercentage;
    private Boolean isCourseCompleted;
}