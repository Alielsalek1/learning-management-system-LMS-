package com.main.lms.dtos;

import com.main.lms.entities.Course;
import com.main.lms.entities.User;

import lombok.Data;

@Data
public class EnrollmentResponseDTO {
    private Long studentId;
    private String studentName;
    private String courseTitle;
    private Long courseId;
    private boolean isConfirmed;

    public EnrollmentResponseDTO() {
    }

    public EnrollmentResponseDTO(User student, Course course, boolean isConfirmed) {
        this.studentId = student.getId();
        this.studentName = student.getName();
        this.courseTitle = course.getTitle();
        this.courseId = course.getId();
        this.isConfirmed = isConfirmed;
    }
}
