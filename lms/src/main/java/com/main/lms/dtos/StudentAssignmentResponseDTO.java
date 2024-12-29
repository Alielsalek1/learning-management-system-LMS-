package com.main.lms.dtos;

import com.main.lms.entities.Course;
import com.main.lms.entities.StudentAssignment;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class StudentAssignmentResponseDTO {
    private Long id;
    private CourseResponseDTO course;
    private UserResponseDTO student;
    private long Grade;
    private double score;
    private String feedback;

    public static StudentAssignmentResponseDTO mapToResponseDTO(StudentAssignment studentAssignment) {
        CourseResponseDTO courseResponseDTO =CourseResponseDTO.mapToResponseDTO(studentAssignment.getCourse());
        UserResponseDTO studentResponseDTO =UserResponseDTO.mapToResponseDTO(studentAssignment.getStudent());
        StudentAssignmentResponseDTO studentAssignmentResponseDTO = new StudentAssignmentResponseDTO();
        studentAssignmentResponseDTO.setId(studentAssignment.getId());
        studentAssignmentResponseDTO.setCourse(courseResponseDTO);
        studentAssignmentResponseDTO.setStudent(studentResponseDTO);
        studentAssignmentResponseDTO.setGrade(studentAssignment.getGrade());
        studentAssignmentResponseDTO.setScore(((double)studentAssignment.getGrade()/(double)studentAssignment.getAssignment().getMaxGrade())*100);
        studentAssignmentResponseDTO.setFeedback(studentAssignment.getFeedback());
        return studentAssignmentResponseDTO;
    }
}
