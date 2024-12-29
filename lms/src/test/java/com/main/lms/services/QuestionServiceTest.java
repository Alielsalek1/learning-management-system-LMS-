package com.main.lms.services;

import com.main.lms.dtos.QuestionRequestDTO;
import com.main.lms.dtos.QuestionResponseDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.Question;
import com.main.lms.entities.User;
import com.main.lms.enums.QuestionType;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private QuestionService questionService;

    private User instructor;
    private Course course;
    private Question question;
    private QuestionRequestDTO questionRequestDTO;

    @BeforeEach
    public void setUp() {
        // Initialize Instructor
        instructor = new User();
        instructor.setId(1L);
        instructor.setName("Instructor");
        instructor.setRole(UserRole.INSTRUCTOR);

        // Initialize Course
        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setInstructor(instructor);

        // Initialize Question
        question = new Question();
        question.setQuestionId(1L);
        question.setCourse(course);
        question.setQuestionContent("What is 2 + 2?");
        question.setType(QuestionType.MCQ);
        question.setAnswer("4");

        // Initialize QuestionRequestDTO
        questionRequestDTO = new QuestionRequestDTO();
        questionRequestDTO.setCourseId(course.getId());
        questionRequestDTO.setContent("What is 2 + 2?");
        questionRequestDTO.setType(QuestionType.MCQ);
        questionRequestDTO.setAnswer("4");
    }

    @Test
    public void testCreateQuestion_Success() {
        // Arrange
        long userId = instructor.getId();

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question savedQuestion = invocation.getArgument(0);
            savedQuestion.setQuestionId(1L);
            return savedQuestion;
        });

        // Act
        Question result = questionService.createQuestion(questionRequestDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals(question.getQuestionId(), result.getQuestionId());
        assertEquals(question.getQuestionContent(), result.getQuestionContent());
        assertEquals(question.getType(), result.getType());
        assertEquals(question.getAnswer(), result.getAnswer());
        assertEquals(course, result.getCourse());

        verify(courseRepository).findById(course.getId());
        verify(questionRepository).save(any(Question.class));
        verify(notificationService).notifyUser(instructor.getId(), "New Question has been posted for course " + course.getTitle());
    }

    @Test
    public void testCreateQuestion_CourseNotFound() {
        // Arrange
        long userId = instructor.getId();
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                questionService.createQuestion(questionRequestDTO, userId)
        );

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(courseRepository).findById(course.getId());
        verify(questionRepository, never()).save(any(Question.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testCreateQuestion_InvalidUser() {
        // Arrange
        long userId = 99L; // Not the instructor
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                questionService.createQuestion(questionRequestDTO, userId)
        );

        assertEquals("You are not the instructor of this course", exception.getMessage());

        verify(courseRepository).findById(course.getId());
        verify(questionRepository, never()).save(any(Question.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testGetQuestionById_Success() {
        // Arrange
        long userId = instructor.getId();
        when(questionRepository.findById(question.getQuestionId())).thenReturn(Optional.of(question));

        // Act
        Question result = questionService.getQuestionById(question.getQuestionId(), userId);

        // Assert
        assertNotNull(result);
        assertEquals(question, result);

        verify(questionRepository).findById(question.getQuestionId());
    }

    @Test
    public void testGetQuestionById_QuestionNotFound() {
        // Arrange
        long userId = instructor.getId();
        when(questionRepository.findById(question.getQuestionId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                questionService.getQuestionById(question.getQuestionId(), userId)
        );

        assertEquals("Question not found with ID: " + question.getQuestionId(), exception.getMessage());

        verify(questionRepository).findById(question.getQuestionId());
    }

    @Test
    public void testGetQuestionById_InvalidUser() {
        // Arrange
        long userId = 99L; // Not the instructor
        when(questionRepository.findById(question.getQuestionId())).thenReturn(Optional.of(question));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                questionService.getQuestionById(question.getQuestionId(), userId)
        );

        assertEquals("You are not the instructor of this course", exception.getMessage());

        verify(questionRepository).findById(question.getQuestionId());
    }

    @Test
    public void testGetFilteredQuestions_Success() {
        // Arrange
        long userId = instructor.getId();
        Optional<QuestionType> questionType = Optional.of(QuestionType.MCQ);
        List<Question> questions = Arrays.asList(question);

        when(courseRepository.existsById(course.getId())).thenReturn(true);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(questionRepository.findByCourseId(course.getId())).thenReturn(questions);

        // Act
        List<Question> result = questionService.getFilteredQuestions(course.getId(), questionType, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(question, result.get(0));

        verify(courseRepository).existsById(course.getId());
        verify(courseRepository).findById(course.getId());
        verify(questionRepository).findByCourseId(course.getId());
    }

    @Test
    public void testGetFilteredQuestions_CourseNotFound() {
        // Arrange
        long userId = instructor.getId();
        Optional<QuestionType> questionType = Optional.empty();

        when(courseRepository.existsById(course.getId())).thenReturn(false);

        // Act & Assert
        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class, () ->
                questionService.getFilteredQuestions(course.getId(), questionType, userId)
        );

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(courseRepository).existsById(course.getId());
    }

    @Test
    public void testGetFilteredQuestions_InvalidUser() {
        // Arrange
        long userId = 99L; // Not the instructor
        Optional<QuestionType> questionType = Optional.empty();

        when(courseRepository.existsById(course.getId())).thenReturn(true);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                questionService.getFilteredQuestions(course.getId(), questionType, userId)
        );

        assertEquals("You are not the instructor of this course", exception.getMessage());

        verify(courseRepository).existsById(course.getId());
        verify(courseRepository).findById(course.getId());
    }

    @Test
    public void testGetRandomQuestions_Success() {
        // Arrange
        Optional<QuestionType> questionType = Optional.empty();
        int count = 1;
        List<Question> questions = Arrays.asList(question);

        when(courseRepository.existsById(course.getId())).thenReturn(true);
        when(questionRepository.findByCourseId(course.getId())).thenReturn(questions);

        // Act
        List<Question> result = questionService.getRandomQuestions(course.getId(), questionType, count);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(question, result.get(0));

        verify(courseRepository).existsById(course.getId());
        verify(questionRepository).findByCourseId(course.getId());
    }

    @Test
    public void testGetRandomQuestions_CourseNotFound() {
        // Arrange
        Optional<QuestionType> questionType = Optional.empty();
        int count = 1;

        when(courseRepository.existsById(course.getId())).thenReturn(false);

        // Act & Assert
        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class, () ->
                questionService.getRandomQuestions(course.getId(), questionType, count)
        );

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(courseRepository).existsById(course.getId());
    }

    @Test
    public void testDeleteQuestion_Success() {
        // Arrange
        long userId = instructor.getId();
        when(questionRepository.existsById(question.getQuestionId())).thenReturn(true);
        when(questionRepository.findById(question.getQuestionId())).thenReturn(Optional.of(question));

        // Act
        questionService.deleteQuestion(question.getQuestionId(), userId);

        // Assert
        verify(questionRepository, times(2)).findById(question.getQuestionId());
        verify(questionRepository).deleteById(question.getQuestionId());
        verify(notificationService).notifyUser(instructor.getId(), "Question Deleted Successfully for course " + course.getTitle());
    }

    @Test
    public void testDeleteQuestion_QuestionNotFound() {
        // Arrange
        long userId = instructor.getId();
        when(questionRepository.existsById(question.getQuestionId())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                questionService.deleteQuestion(question.getQuestionId(), userId)
        );

        assertEquals("Question not found with ID: " + question.getQuestionId(), exception.getMessage());

        verify(questionRepository).existsById(question.getQuestionId());
        verify(questionRepository, never()).findById(question.getQuestionId());
        verify(questionRepository, never()).deleteById(question.getQuestionId());
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testDeleteQuestion_InvalidUser() {
        // Arrange
        long userId = 99L; // Not the instructor
        when(questionRepository.existsById(question.getQuestionId())).thenReturn(true);
        when(questionRepository.findById(question.getQuestionId())).thenReturn(Optional.of(question));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                questionService.deleteQuestion(question.getQuestionId(), userId)
        );

        assertEquals("You are not the instructor of this course", exception.getMessage());

        verify(questionRepository).existsById(question.getQuestionId());
        verify(questionRepository).findById(question.getQuestionId());
        verify(questionRepository, never()).deleteById(question.getQuestionId());
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testMapToResponseDTO() {
        // Act
        QuestionResponseDTO dto = questionService.mapToResponseDTO(question);

        // Assert
        assertNotNull(dto);
        assertEquals(question.getQuestionId(), dto.getQuestionId());
        assertEquals(question.getQuestionContent(), dto.getContent());
        assertEquals(question.getType(), dto.getType());
        assertEquals(question.getAnswer(), dto.getAnswer());
        assertEquals(course.getTitle(), dto.getCourseTitle());
    }
}