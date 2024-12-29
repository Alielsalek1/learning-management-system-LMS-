package com.main.lms.repositories;

import com.main.lms.entities.Course;
import com.main.lms.entities.Quiz;
import com.main.lms.entities.StudentQuiz;
import com.main.lms.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentQuizRepository extends JpaRepository<StudentQuiz, Long> {
    boolean existsStudentQuizByStudentIdAndQuiz(Long studentId, Quiz quiz);
    List<StudentQuiz> findByStudentId(Long studentId);
    List<StudentQuiz> findByQuiz_Course_Id(Long courseId);
    List<StudentQuiz> findByQuiz(Quiz quiz);
    Optional<StudentQuiz> findByStudentAndQuiz(User student, Quiz quiz);
    List<StudentQuiz> findByStudentAndQuiz_Course(User student, Course course);
}
