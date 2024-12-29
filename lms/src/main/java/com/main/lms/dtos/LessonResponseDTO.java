package com.main.lms.dtos;

import com.main.lms.entities.Lesson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonResponseDTO {
    private Long lessonId;
    private String CourseTitle;

    public LessonResponseDTO(Lesson lesson) {
        this.lessonId = lesson.getLessonId();
        this.CourseTitle = lesson.getCourse().getTitle();
    }
}
