package com.main.lms.repositories;

import com.main.lms.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrolledCourseRepository extends JpaRepository<EnrolledCourse, Long> {
    List<EnrolledCourse> findByStudent(User student);
    List<EnrolledCourse> findByCourse(Course course);
    List<EnrolledCourse> findByStudentAndCourse(User student, Course course);
}