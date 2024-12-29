package com.main.lms.repositories;

import com.main.lms.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentsLessonRepository extends JpaRepository<StudentLesson, Long> {
    List<StudentLesson> findByStudentId(Long studentId);

    List<StudentLesson> findByLesson_Course_Id(Long courseId);

    Optional<StudentLesson> findByStudentAndLesson(User student, Lesson lesson);

    List<StudentLesson> findByStudentAndLesson_Course(User student, Course course);

}

