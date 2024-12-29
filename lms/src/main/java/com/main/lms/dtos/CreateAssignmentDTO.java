package com.main.lms.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAssignmentDTO {
    @NotNull
    long courseId;

    @NotNull
    int maxGrade;

    @NotNull
    String instructions;
}
