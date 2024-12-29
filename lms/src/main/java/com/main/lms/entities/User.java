package com.main.lms.entities;

import com.main.lms.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // ENUM Role for Admin, Instructor, Student

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;
}
