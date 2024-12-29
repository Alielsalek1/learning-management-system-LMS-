package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.*;
import com.main.lms.entities.*;
import com.main.lms.enums.UserRole;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.services.StudentLessonService;
import com.main.lms.utility.SessionIdUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(StudentLessonController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudentLessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentLessonService studentLessonService;

    @MockBean
    private SessionIdUtility sessionIdUtility;

    @MockBean
    private CourseRepository courseRepository;

    private ObjectMapper objectMapper;

    private User student;
    private User instructor;
    private CustomUserDetails customUserDetailsStudent;
    private CustomUserDetails customUserDetailsInstructor;
    private Course course;
    private Lesson lesson;
    private StudentLesson studentLesson;
    private CreateStudentLessonDTO createStudentLessonDTO;
    private EnrollmentLessonResponseDTO enrollmentLessonResponseDTO;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();

        // Initialize student
        student = new User();
        student.setId(1L);
        student.setName("John Doe");
        student.setRole(UserRole.STUDENT);

        // Initialize instructor
        instructor = new User();
        instructor.setId(2L);
        instructor.setName("Jane Smith");
        instructor.setRole(UserRole.INSTRUCTOR);

        // Initialize CustomUserDetails
        customUserDetailsStudent = new CustomUserDetails(student);
        customUserDetailsInstructor = new CustomUserDetails(instructor);

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

        // Initialize StudentLesson
        studentLesson = new StudentLesson();
        studentLesson.setStudent(student);
        studentLesson.setLesson(lesson);

        // Initialize CreateStudentLessonDTO
        createStudentLessonDTO = new CreateStudentLessonDTO();
        createStudentLessonDTO.setLessonId(lesson.getLessonId());
        createStudentLessonDTO.setOtp("123456");

        // Initialize EnrollmentLessonResponseDTO
        enrollmentLessonResponseDTO = new EnrollmentLessonResponseDTO(studentLesson);
    }

    // Test for createStudentLesson method - Success case
    @Test
    public void testCreateStudentLesson_Success() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetailsStudent);
        when(studentLessonService.createStudentLesson(any(CreateStudentLessonDTO.class), eq(student)))
                .thenReturn(studentLesson);

        // Act
        mockMvc.perform(post("/student-lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStudentLessonDTO)))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student Lesson created successfully"))
                .andExpect(jsonPath("$.data.lessonId").value(1));
    }

    // Test for createStudentLesson - RuntimeException
    @Test
    public void testCreateStudentLesson_RuntimeException() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetailsStudent);
        when(studentLessonService.createStudentLesson(any(CreateStudentLessonDTO.class), eq(student)))
                .thenThrow(new RuntimeException("Invalid OTP"));

        // Act
        mockMvc.perform(post("/student-lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createStudentLessonDTO)))
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create Student Lesson"))
                .andExpect(jsonPath("$.errors[0]").value("Invalid OTP"));
    }

    // Test for createStudentLesson - Exception
   

    // Test for getLessonsByStudent - Success
    @Test
    public void testGetLessonsByStudent_Success() throws Exception {
        // Arrange
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson);
        List<EnrollmentLessonResponseDTO> responseDTOs = Arrays.asList(enrollmentLessonResponseDTO);

        when(studentLessonService.getLessonsByStudent(eq(student.getId()))).thenReturn(studentLessons);

        // Act
        mockMvc.perform(get("/student-lessons/students/{studentId}", student.getId()))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lessonId").value(1));
    }

    // Test for getLessonsByStudentInCourse - Success
    @Test
    public void testGetLessonsByStudentInCourse_Success() throws Exception {
        // Arrange
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson);
        List<EnrollmentLessonResponseDTO> responseDTOs = Arrays.asList(enrollmentLessonResponseDTO);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetailsInstructor);
        when(studentLessonService.getLessonsByStudentInCourse(eq(student.getId()), eq(course.getId()), eq(instructor.getId())))
                .thenReturn(studentLessons);

        // Act
        mockMvc.perform(get("/student-lessons/students/{studentId}/courses/{courseId}",
                student.getId(), course.getId()))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student Lessons in Course"))
                .andExpect(jsonPath("$.data[0].lessonId").value(1));
    }

   

    // Test for meGetLessonsByStudent - Success
    @Test
    public void testMeGetLessonsByStudent_Success() throws Exception {
        // Arrange
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson);
        List<EnrollmentLessonResponseDTO> responseDTOs = Arrays.asList(enrollmentLessonResponseDTO);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetailsStudent);
        when(studentLessonService.getLessonsByStudent(eq(student.getId()))).thenReturn(studentLessons);

        // Act
        mockMvc.perform(get("/student-lessons/students/me"))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student Lessons"))
                .andExpect(jsonPath("$.data[0].lessonId").value(1));
    }

    

    // Test for meGetStudentLessonsInCourse - Success
    @Test
    public void testMeGetStudentLessonsInCourse_Success() throws Exception {
        // Arrange
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson);
        List<EnrollmentLessonResponseDTO> responseDTOs = Arrays.asList(enrollmentLessonResponseDTO);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetailsStudent);
        when(courseRepository.findById(eq(course.getId()))).thenReturn(Optional.of(course));
        when(studentLessonService.getLessonsByStudentInCourse(eq(student.getId()), eq(course.getId()), eq(instructor.getId())))
                .thenReturn(studentLessons);

        // Act
        mockMvc.perform(get("/student-lessons/students/me/courses/{courseId}", course.getId()))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student Lessons in Course"))
                .andExpect(jsonPath("$.data[0].lessonId").value(1));
    }

    // Test for meGetStudentLessonsInCourse - Course Not Found
    @Test
    public void testMeGetStudentLessonsInCourse_CourseNotFound() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetailsStudent);
        when(courseRepository.findById(eq(course.getId()))).thenReturn(Optional.empty());

        // Act
        mockMvc.perform(get("/student-lessons/students/me/courses/{courseId}", course.getId()))
                // Assert
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to get Student Lessons"))
                .andExpect(jsonPath("$.errors[0]").value("Course not found with ID: " + course.getId()));
    }

}