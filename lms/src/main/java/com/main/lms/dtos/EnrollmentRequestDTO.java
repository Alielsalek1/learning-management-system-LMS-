package com.main.lms.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequestDTO {
    @NotNull
    private Long courseId;

    private Boolean isCompleted  = false;
}
