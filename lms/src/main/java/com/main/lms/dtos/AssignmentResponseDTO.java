package com.main.lms.dtos;

import com.main.lms.entities.Assignment;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AssignmentResponseDTO {
    private Long id;
    private CourseResponseDTO course;
    private String instructions;
    private int maxGrade;

    public static AssignmentResponseDTO mapToResponseDTO(Assignment assignment) {
        CourseResponseDTO courseResponseDTO = CourseResponseDTO.mapToResponseDTO(assignment.getCourse());
        String instructions = assignment.getInstructions();
        int maxGrade = assignment.getMaxGrade();
        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
        assignmentResponseDTO.setId(assignment.getAssignmentId());
        assignmentResponseDTO.setCourse(courseResponseDTO);
        assignmentResponseDTO.setInstructions(instructions);
        assignmentResponseDTO.setMaxGrade(maxGrade);
        return assignmentResponseDTO;
    }
}
