package com.main.lms.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "instructor_id", nullable = false, insertable = false, updatable = false)
    private Long instructorId;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false, referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User instructor;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String duration;

    private String description;

    @ElementCollection
    @CollectionTable(name = "course_materials", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "material")
    private List<String> materials;

}
