package com.main.lms.dtos;

import com.main.lms.entities.StudentLesson;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EnrollmentLessonResponseDTO {

    private Long studentId;
    private Long lessonId;
    private String studentName;
    private String courseName;

    public EnrollmentLessonResponseDTO(StudentLesson studentLesson) {
        this.studentId = studentLesson.getStudent().getId();
        this.lessonId = studentLesson.getLesson().getLessonId();
        this.studentName = studentLesson.getStudent().getName();
        this.courseName = studentLesson.getLesson().getCourse().getTitle();
    }
}
