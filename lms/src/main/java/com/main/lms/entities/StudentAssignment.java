package com.main.lms.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Data
public class StudentAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "assignment_id", nullable = false, insertable = false, updatable = false)
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Assignment assignment;

    @Column(name = "student_id", nullable = false, insertable = false, updatable = false)
    private Long studentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User student;

    @ElementCollection
    @CollectionTable(name = "student_assignment_files", joinColumns = @JoinColumn(name = "student_assignment_id"))
    @Column(name = "file_name")
    private List<String> fileNames;

    @Column(name = "course_id", nullable = false, insertable = false, updatable = false)
    private Long courseId;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Course course;

    @Column(nullable = true)
    private String feedback;

    @Column
    private long Grade;
}
