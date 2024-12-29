package com.main.lms.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeAssignmentDTO {

    @NotNull
    long grade;
    String feedback;
}
