package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.*;
import com.main.lms.entities.*;
import com.main.lms.enums.QuestionType;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.*;
import com.main.lms.services.AnalyticsService;
import com.main.lms.services.QuestionService;
import com.main.lms.services.ReportService;
import com.main.lms.utility.SessionIdUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private SessionIdUtility sessionIdUtility;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private ReportService reportService;

    private ObjectMapper objectMapper;

    private User instructor;
    private CustomUserDetails customUserDetails;
    private QuestionRequestDTO questionRequestDTO;
    private QuestionResponseDTO questionResponseDTO;
    private Question question;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();

        // Initialize instructor
        instructor = new User();
        instructor.setId(1L);
        instructor.setName("Instructor");
        instructor.setRole(UserRole.INSTRUCTOR);

        // Initialize CustomUserDetails
        customUserDetails = new CustomUserDetails(instructor);

        // Initialize QuestionRequestDTO
        questionRequestDTO = new QuestionRequestDTO();
        questionRequestDTO.setCourseId(1L);
        questionRequestDTO.setContent("What is 2 + 2?");
        questionRequestDTO.setType(QuestionType.MCQ);
        questionRequestDTO.setAnswer("4");

        // Initialize QuestionResponseDTO
        questionResponseDTO = new QuestionResponseDTO();
        questionResponseDTO.setQuestionId(1L);
        questionResponseDTO.setContent("What is 2 + 2?");
        questionResponseDTO.setType(QuestionType.MCQ);
        questionResponseDTO.setAnswer("4");
        questionResponseDTO.setCourseTitle("Mathematics");

        // Initialize Question entity
        question = new Question();
        question.setQuestionId(1L);
        question.setQuestionContent("What is 2 + 2?");
        question.setType(QuestionType.MCQ);
        question.setAnswer("4");
    }

    // Test for createQuestion method - Success case
    @Test
    public void testCreateQuestion_Success() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.createQuestion(any(QuestionRequestDTO.class), eq(instructor.getId())))
                .thenReturn(question);
        when(questionService.mapToResponseDTO(any(Question.class))).thenReturn(questionResponseDTO);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequestDTO)));

        // Assert
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Question created successfully"))
                .andExpect(jsonPath("$.data.questionId").value(1));
    }

    // Test for createQuestion - Unauthorized user
    @Test
    public void testCreateQuestion_Unauthorized() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequestDTO)));

        // Assert
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    // Test for createQuestion - InvalidUser exception
    @Test
    public void testCreateQuestion_InvalidUser() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.createQuestion(any(QuestionRequestDTO.class), eq(instructor.getId())))
                .thenThrow(new InvalidUser("User is not authorized"));

        // Act
        ResultActions resultActions = mockMvc.perform(post("/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequestDTO)));

        // Assert
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized"));
    }

    // Test for createQuestion - ResourceNotFoundException
    @Test
    public void testCreateQuestion_ResourceNotFound() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.createQuestion(any(QuestionRequestDTO.class), eq(instructor.getId())))
                .thenThrow(new ResourceNotFoundException("Course not found"));

        // Act
        ResultActions resultActions = mockMvc.perform(post("/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequestDTO)));

        // Assert
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.errors[0]").value("Course not found"));
    }

    // Test for getQuestionsByCourseId - Success
    @Test
    public void testGetQuestionsByCourseId_Success() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        List<QuestionResponseDTO> questions = Arrays.asList(questionResponseDTO);
        when(questionService.getFilteredQuestions(eq(1L), any(Optional.class), eq(instructor.getId())))
                .thenReturn(Arrays.asList(question));
        when(questionService.mapToResponseDTO(any(Question.class))).thenReturn(questionResponseDTO);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/course/1"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Questions fetched successfully"))
                .andExpect(jsonPath("$.data[0].questionId").value(1));
    }

    // Test for getQuestionsByCourseId - Unauthorized
    @Test
    public void testGetQuestionsByCourseId_Unauthorized() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/course/1"));

        // Assert
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    // Test for getQuestionsByCourseId - CourseNotFoundException
    @Test
    public void testGetQuestionsByCourseId_CourseNotFound() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.getFilteredQuestions(eq(1L), any(Optional.class), eq(instructor.getId())))
                .thenThrow(new CourseNotFoundException("Course not found"));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/course/1"));

        // Assert
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.errors[0]").value("Course not found"));
    }

    // Test for getQuestionsByCourseId - InvalidUser
    @Test
    public void testGetQuestionsByCourseId_InvalidUser() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.getFilteredQuestions(eq(1L), any(Optional.class), eq(instructor.getId())))
                .thenThrow(new InvalidUser("User is not authorized"));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/course/1"));

        // Assert
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized"))
                .andExpect(jsonPath("$.errors[0]").value("User is not authorized"));
    }

    // Test for getQuestionById - Success
    @Test
    public void testGetQuestionById_Success() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.getQuestionById(eq(1L), eq(instructor.getId()))).thenReturn(question);
        when(questionService.mapToResponseDTO(any(Question.class))).thenReturn(questionResponseDTO);

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/1"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Question retrieved successfully"))
                .andExpect(jsonPath("$.data.questionId").value(1));
    }

    // Test for getQuestionById - Unauthorized
    @Test
    public void testGetQuestionById_Unauthorized() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/1"));

        // Assert
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    // Test for getQuestionById - ResourceNotFoundException
    @Test
    public void testGetQuestionById_ResourceNotFound() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.getQuestionById(eq(1L), eq(instructor.getId())))
                .thenThrow(new ResourceNotFoundException("Question not found"));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/1"));

        // Assert
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.errors[0]").value("Question not found"));
    }

    // Test for getQuestionById - InvalidUser
    @Test
    public void testGetQuestionById_InvalidUser() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        when(questionService.getQuestionById(eq(1L), eq(instructor.getId())))
                .thenThrow(new InvalidUser("User is not authorized"));

        // Act
        ResultActions resultActions = mockMvc.perform(get("/questions/1"));

        // Assert
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized"))
                .andExpect(jsonPath("$.errors[0]").value("User is not authorized"));
    }

    // Test for deleteQuestion - Success
    @Test
    public void testDeleteQuestion_Success() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        doNothing().when(questionService).deleteQuestion(eq(1L), eq(instructor.getId()));

        // Act
        ResultActions resultActions = mockMvc.perform(delete("/questions/1"));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Question deleted successfully"));
    }

    // Test for deleteQuestion - Unauthorized
    @Test
    public void testDeleteQuestion_Unauthorized() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        // Act
        ResultActions resultActions = mockMvc.perform(delete("/questions/1"));

        // Assert
        resultActions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    // Test for deleteQuestion - InvalidUser
    @Test
    public void testDeleteQuestion_InvalidUser() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        doThrow(new InvalidUser("User is not authorized")).when(questionService).deleteQuestion(eq(1L), eq(instructor.getId()));

        // Act
        ResultActions resultActions = mockMvc.perform(delete("/questions/1"));

        // Assert
        resultActions.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("no"));
    }

    // Test for deleteQuestion - ResourceNotFoundException
    @Test
    public void testDeleteQuestion_ResourceNotFound() throws Exception {
        // Arrange
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(customUserDetails);
        doThrow(new ResourceNotFoundException("Question not found")).when(questionService).deleteQuestion(eq(1L), eq(instructor.getId()));

        // Act
        ResultActions resultActions = mockMvc.perform(delete("/questions/1"));

        // Assert
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.errors[0]").value("Question not found"));
    }
}