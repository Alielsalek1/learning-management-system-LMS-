package com.main.lms.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonRequestDTO {
    @NotBlank
    public String otp;

    @NotBlank
    public Long courseId;
}
