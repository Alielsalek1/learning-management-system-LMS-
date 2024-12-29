package com.main.lms.repositories;

import com.main.lms.entities.Assignment;
import com.main.lms.entities.Course;
import com.main.lms.entities.StudentAssignment;
import com.main.lms.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAssignmentRepository extends JpaRepository<StudentAssignment, Long> {
    List<StudentAssignment> findByStudentId(Long studentId);
    List<StudentAssignment> findByCourse(Course course);
    List<StudentAssignment> findByStudent(User student);
    List<StudentAssignment>  findByCourse_Id(Long courseId);
    Optional<StudentAssignment> findByCourseAndStudent(Course course, User student);
    Optional<StudentAssignment> findByAssignmentAndStudent(Assignment assignment, User student);
    List<StudentAssignment> findByStudentAndCourse(User student, Course course);
}
