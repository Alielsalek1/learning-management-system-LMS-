package com.main.lms.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStudentAssignmentDTO {
    @NotNull
    private Long assignmentId;
    @NotNull
    private Long courseId;
}
