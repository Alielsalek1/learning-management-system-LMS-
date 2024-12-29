 package com.main.lms.repositories;

 import com.main.lms.entities.Assignment;
 import com.main.lms.entities.Course;
 import org.springframework.data.jpa.repository.JpaRepository;

 import java.util.List;


 public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourse(Course course);
 }