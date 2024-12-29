package com.main.lms.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
public class StudentLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentLessonId;

    @Column(name = "student_id", nullable = false, insertable = false, updatable = false)
    private Long studentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User student;

    @Column(name = "lesson_id", nullable = false, insertable = false, updatable = false)
    private Long lessonId;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lesson lesson;
}