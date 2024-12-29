package com.main.lms.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
public class EnrolledCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, insertable = false, updatable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false, insertable = false, updatable = false)
    private Long courseId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable =  false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Course course;

    private Boolean isConfirmed = false;

    private Boolean isCompleted = false;
}
