package com.main.lms.services;

import com.main.lms.entities.*;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.*;
import com.main.lms.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private StudentQuizRepository studentQuizRepository;

    @Mock
    private QuestionService questionService;

    @Mock
    private EnrolledCourseRepository enrolledCourseRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private QuizService quizService;

    private User instructor;
    private User student;
    private Course course;
    private Quiz quiz;
    private Question question;

    @BeforeEach
    public void setUp() {
        // Initialize instructor
        instructor = new User();
        instructor.setId(1L);
        instructor.setName("Instructor");
        instructor.setRole(UserRole.INSTRUCTOR);

        // Initialize student
        student = new User();
        student.setId(2L);
        student.setName("Student");
        student.setRole(UserRole.STUDENT);

        // Initialize course
        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setInstructor(instructor);

        // Initialize question
        question = new Question();
        question.setQuestionId(1L);
        question.setCourse(course);
        question.setQuestionContent("What is 2 + 2?");
        question.setAnswer("4");

        // Initialize quiz
        quiz = new Quiz();
        quiz.setQuizId(1L);
        quiz.setCourse(course);
        quiz.setQuestions(Arrays.asList(question));
    }

    @Test
    public void testGetQuizById_InstructorSuccess() {
        // Arrange
        Long quizId = quiz.getQuizId();
        User user = instructor;

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        // Act
        Quiz result = quizService.getQuizById(quizId, user);

        // Assert
        assertNotNull(result);
        assertEquals(quiz, result);
        verify(quizRepository).findById(quizId);
    }

    @Test
    public void testGetQuizById_StudentSuccess() {
        // Arrange
        Long quizId = quiz.getQuizId();
        User user = student;

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(enrolledCourseRepository.findByStudentAndCourse(user, course)).thenReturn(Collections.singletonList(new EnrolledCourse()));

        // Act
        Quiz result = quizService.getQuizById(quizId, user);

        // Assert
        assertNotNull(result);
        assertEquals(quiz, result);
        verify(quizRepository).findById(quizId);
        verify(enrolledCourseRepository).findByStudentAndCourse(user, course);
    }

    @Test
    public void testGetQuizById_QuizNotFound() {
        // Arrange
        Long quizId = 999L; // Non-existent quiz
        User user = instructor;

        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            quizService.getQuizById(quizId, user);
        });

        assertEquals("Quiz not found with ID: " + quizId, exception.getMessage());

        verify(quizRepository).findById(quizId);
    }

    @Test
    public void testGetQuizById_UnauthorizedUser() {
        // Arrange
        Long quizId = quiz.getQuizId();
        User user = new User();
        user.setId(3L); // Another user not instructor nor student
        user.setName("Unauthorized User");
        user.setRole(UserRole.STUDENT);

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(enrolledCourseRepository.findByStudentAndCourse(user, course)).thenReturn(Collections.emptyList());

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () -> {
            quizService.getQuizById(quizId, user);
        });

        assertEquals("You are not authorized", exception.getMessage());

        verify(quizRepository).findById(quizId);
        verify(enrolledCourseRepository).findByStudentAndCourse(user, course);
    }

    // Continue with other test methods, adjusting the return types accordingly.

    // Rest of the test methods ...

}