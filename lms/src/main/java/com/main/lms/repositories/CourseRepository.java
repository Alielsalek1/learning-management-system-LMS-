package com.main.lms.repositories;

import com.main.lms.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);

    @NonNull
    Optional<Course> findById(@NonNull Long courseId);
}