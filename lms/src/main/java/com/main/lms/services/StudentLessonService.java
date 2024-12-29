package com.main.lms.services;

import com.main.lms.dtos.CreateStudentLessonDTO;
import com.main.lms.entities.*;
import com.main.lms.entities.StudentLesson;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.repositories.LessonRepository;
import com.main.lms.repositories.StudentsLessonRepository;
import com.main.lms.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentLessonService {

    private final StudentsLessonRepository studentLessonsRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;

    boolean exists(User user, Lesson lesson) {
        Optional<StudentLesson> x = studentLessonsRepository.findByStudentAndLesson(user, lesson);
        if (x.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    // Create StudentLesson
    public StudentLesson createStudentLesson(CreateStudentLessonDTO createStudentLessonDTO, User student) {
        // Retrieve the lesson
        Lesson lesson = lessonRepository.findById(createStudentLessonDTO.getLessonId())
                .orElseThrow(() -> new RuntimeException(
                        "Lesson not found with ID: " + createStudentLessonDTO.getLessonId()));

        if (enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), lesson.getCourse().getId()).isEmpty()) {
            throw new CourseNotFoundException("Student is not enrolled in this course");
        }

        if (!createStudentLessonDTO.getOtp().equals(lesson.getOtp())) {
            throw new RuntimeException("OTP is invalid");
        }

        if (exists(student, lesson)) {
            throw new RuntimeException("Student is already enrolled in this lesson");
        }

        // Create StudentLesson entity
        StudentLesson studentLesson = new StudentLesson();
        studentLesson.setStudent(student);
        studentLesson.setLesson(lesson);

        return studentLessonsRepository.save(studentLesson);
    }

    // Get a StudentLesson by ID
    public StudentLesson getStudentLessonById(Long id) {

        return studentLessonsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudentLesson not found with ID: " + id));
    }

    // Get All Lessons by Student
    public List<StudentLesson> getLessonsByStudent(Long studentId) {
        return studentLessonsRepository.findByStudentId(studentId);
    }

    public List<StudentLesson> getLessonsByStudentInCourse(Long studentId, Long courseId, Long instructorId) {

        List<StudentLesson> studentLessons = studentLessonsRepository.findByStudentId(studentId);
        return studentLessons.stream()
                .filter(sl -> sl.getLesson().getCourse().getId().equals(courseId) && sl.getLesson().getCourse().getInstructor().getId().equals(instructorId))
                .collect(Collectors.toList());
    }

   // Delete StudentLesson
   public void deleteStudentLessonById(Long id) {
       StudentLesson sl = getStudentLessonById(id);
       studentLessonsRepository.delete(sl);
   }
}
