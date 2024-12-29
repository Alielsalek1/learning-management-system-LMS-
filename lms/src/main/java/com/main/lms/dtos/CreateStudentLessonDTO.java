package com.main.lms.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStudentLessonDTO {
    @NotNull
    private Long lessonId;

    @NotNull
    private String otp;
}
