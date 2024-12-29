package com.main.lms.services;

import com.main.lms.dtos.CreateStudentLessonDTO;
import com.main.lms.dtos.EnrollmentResponseDTO;
import com.main.lms.entities.*;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.repositories.LessonRepository;
import com.main.lms.repositories.StudentsLessonRepository;
import com.main.lms.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StudentLessonServiceTest {

    @Mock
    private StudentsLessonRepository studentLessonsRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private StudentLessonService studentLessonService;

    private User student;
    private User instructor;
    private Course course;
    private Lesson lesson;
    private CreateStudentLessonDTO createStudentLessonDTO;
    private StudentLesson studentLesson;
    private EnrolledCourse enrollment;

    @BeforeEach
    public void setUp() {
        // Initialize student
        student = new User();
        student.setId(1L);
        student.setName("John Doe");

        // Initialize instructor
        instructor = new User();
        instructor.setId(2L);
        instructor.setName("Jane Smith");

        // Initialize course
        course = new Course();
        course.setId(1L);
        course.setTitle("Math 101");
        course.setInstructor(instructor);

        // Initialize lesson
        lesson = new Lesson();
        lesson.setLessonId(1L);
        lesson.setCourse(course);
        lesson.setOtp("123456");

        // Initialize CreateStudentLessonDTO
        createStudentLessonDTO = new CreateStudentLessonDTO();
        createStudentLessonDTO.setLessonId(lesson.getLessonId());
        createStudentLessonDTO.setOtp("123456");

        // Initialize StudentLesson
        studentLesson = new StudentLesson();
        studentLesson.setStudentId(1L);
        studentLesson.setStudent(student);
        studentLesson.setLesson(lesson);

        // Initialize Enrollment
        enrollment = new EnrolledCourse();
        enrollment.setId(1L);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
    }

    // Test for createStudentLesson method - Success case
    @Test
    public void testCreateStudentLesson_Success() {
        // Arrange
        when(lessonRepository.findById(lesson.getLessonId())).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), lesson.getCourse().getId()))
                .thenReturn(Optional.of(new EnrollmentResponseDTO()));
        when(studentLessonsRepository.findByStudentAndLesson(student, lesson)).thenReturn(Optional.empty());
        when(studentLessonsRepository.save(any(StudentLesson.class))).thenAnswer(invocation -> {
            StudentLesson sl = invocation.getArgument(0);
            sl.setStudentId(1L);
            return sl;
        });

        // Act
        StudentLesson result = studentLessonService.createStudentLesson(createStudentLessonDTO, student);

        // Assert
        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(lesson, result.getLesson());
    }

    // Test for createStudentLesson - Lesson not found
    @Test
    public void testCreateStudentLesson_LessonNotFound() {
        // Arrange
        when(lessonRepository.findById(lesson.getLessonId())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentLessonService.createStudentLesson(createStudentLessonDTO, student);
        });

        assertEquals("Lesson not found with ID: " + lesson.getLessonId(), exception.getMessage());
    }

    // Test for createStudentLesson - Student not enrolled in course
    @Test
    public void testCreateStudentLesson_StudentNotEnrolled() {
        // Arrange
        when(lessonRepository.findById(lesson.getLessonId())).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), lesson.getCourse().getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(CourseNotFoundException.class, () -> {
            studentLessonService.createStudentLesson(createStudentLessonDTO, student);
        });

        assertEquals("Student is not enrolled in this course", exception.getMessage());
    }

    // Test for createStudentLesson - Invalid OTP
    @Test
    public void testCreateStudentLesson_InvalidOtp() {
        // Arrange
        createStudentLessonDTO.setOtp("000000"); // Invalid OTP
        when(lessonRepository.findById(lesson.getLessonId())).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), lesson.getCourse().getId()))
                .thenReturn(Optional.of(new EnrollmentResponseDTO()));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentLessonService.createStudentLesson(createStudentLessonDTO, student);
        });

        assertEquals("OTP is invalid", exception.getMessage());
    }

    // Test for createStudentLesson - Student already enrolled in lesson
    @Test
    public void testCreateStudentLesson_StudentAlreadyEnrolledInLesson() {
        // Arrange
        when(lessonRepository.findById(lesson.getLessonId())).thenReturn(Optional.of(lesson));
        when(enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), lesson.getCourse().getId()))
                .thenReturn(Optional.of(new EnrollmentResponseDTO()));
        when(studentLessonsRepository.findByStudentAndLesson(student, lesson))
                .thenReturn(Optional.of(studentLesson));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentLessonService.createStudentLesson(createStudentLessonDTO, student);
        });

        assertEquals("Student is already enrolled in this lesson", exception.getMessage());
    }

    // Test for getStudentLessonById - Not found
    @Test
    public void testGetStudentLessonById_NotFound() {
        // Arrange
        when(studentLessonsRepository.findById(studentLesson.getStudentLessonId())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentLessonService.getStudentLessonById(studentLesson.getStudentLessonId());
        });

        assertEquals("StudentLesson not found with ID: " + studentLesson.getStudentLessonId(), exception.getMessage());
    }

    // Test for getLessonsByStudent - Success
    @Test
    public void testGetLessonsByStudent_Success() {
        // Arrange
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson);
        when(studentLessonsRepository.findByStudentId(student.getId())).thenReturn(studentLessons);

        // Act
        List<StudentLesson> result = studentLessonService.getLessonsByStudent(student.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(studentLesson, result.get(0));
    }

    // Test for getLessonsByStudentInCourse - Success
    @Test
    public void testGetLessonsByStudentInCourse_Success() {
        // Arrange
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson);
        when(studentLessonsRepository.findByStudentId(student.getId())).thenReturn(studentLessons);

        // Act
        List<StudentLesson> result = studentLessonService.getLessonsByStudentInCourse(
                student.getId(), course.getId(), instructor.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(studentLesson, result.get(0));
    }

    // Test for getLessonsByStudentInCourse - No matching lessons
    @Test
    public void testGetLessonsByStudentInCourse_NoMatchingLessons() {
        // Arrange
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson);
        when(studentLessonsRepository.findByStudentId(student.getId())).thenReturn(studentLessons);

        // Act
        List<StudentLesson> result = studentLessonService.getLessonsByStudentInCourse(
                student.getId(), 999L, instructor.getId()); // Non-matching courseId

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Test for deleteStudentLessonById - Success
    @Test
    public void testDeleteStudentLessonById_Success() {
        // Arrange
        when(studentLessonsRepository.findById(studentLesson.getStudentLessonId())).thenReturn(Optional.of(studentLesson));
        doNothing().when(studentLessonsRepository).delete(studentLesson);

        // Act
        studentLessonService.deleteStudentLessonById(studentLesson.getStudentLessonId());

        // Assert
        verify(studentLessonsRepository, times(1)).delete(studentLesson);
    }

    // Test for deleteStudentLessonById - Not found
    @Test
    public void testDeleteStudentLessonById_NotFound() {
        // Arrange
        when(studentLessonsRepository.findById(studentLesson.getStudentLessonId())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentLessonService.deleteStudentLessonById(studentLesson.getStudentLessonId());
        });

        assertEquals("StudentLesson not found with ID: " + studentLesson.getStudentLessonId(), exception.getMessage());
        verify(studentLessonsRepository, times(0)).delete(any(StudentLesson.class));
    }
}