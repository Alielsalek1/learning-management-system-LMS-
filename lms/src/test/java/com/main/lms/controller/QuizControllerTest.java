package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.*;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.Quiz;
import com.main.lms.entities.User;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.QuizService;
import com.main.lms.utility.SessionIdUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @MockBean
    private SessionIdUtility sessionIdUtility;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        // Set other necessary user details if needed
        userDetails = new CustomUserDetails(user);
    }

    @Test
    public void testGetQuizById_Success() throws Exception {
        Long quizId = 1L;
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setQuizId(quizId);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(quizService.getQuizById(eq(quizId), eq(user))).thenReturn(new Quiz());
        when(quizService.mapToResponseDTO(any(Quiz.class))).thenReturn(quizDTO);

        mockMvc.perform(get("/quizzes/{quizId}", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz fetched successfully"))
                .andExpect(jsonPath("$.data.quizId").value(quizId));
    }

    @Test
    public void testGetQuizById_Unauthenticated() throws Exception {
        Long quizId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        mockMvc.perform(get("/quizzes/{quizId}", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    public void testGetQuizById_InvalidUser() throws Exception {
        Long quizId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new InvalidUser("User is not authorized")).when(quizService).getQuizById(eq(quizId), eq(user));

        mockMvc.perform(get("/quizzes/{quizId}", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized"));
    }

    @Test
    public void testGetQuizById_NotFound() throws Exception {
        Long quizId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new ResourceNotFoundException("Quiz not found")).when(quizService).getQuizById(eq(quizId), eq(user));

        mockMvc.perform(get("/quizzes/{quizId}", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Quiz not found"))
                .andExpect(jsonPath("$.errors[0]").value("Quiz not found"));
    }

    @Test
    public void testGenerateQuizForCourse_Success() throws Exception {
        GenerateQuizRequestDTO requestDTO = new GenerateQuizRequestDTO();
        // Set properties of requestDTO as needed
        requestDTO.setCourseId(1L);
        requestDTO.setQuestionCount(10);

        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setQuizId(1L);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(quizService.generateQuizForCourse(eq(requestDTO), eq(user))).thenReturn(new Quiz());
        when(quizService.mapToResponseDTO(any(Quiz.class))).thenReturn(quizDTO);

        mockMvc.perform(post("/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quiz generated successfully"))
                .andExpect(jsonPath("$.data.quizId").value(1));
    }

    @Test
    public void testGenerateQuizForCourse_Unauthenticated() throws Exception {
        GenerateQuizRequestDTO requestDTO = new GenerateQuizRequestDTO();
        // Set properties of requestDTO as needed

        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        mockMvc.perform(post("/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    public void testGenerateQuizForCourse_InvalidUser() throws Exception {
        GenerateQuizRequestDTO requestDTO = new GenerateQuizRequestDTO();
        // Set properties of requestDTO as needed

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new InvalidUser("User is not authorized")).when(quizService).generateQuizForCourse(eq(requestDTO), eq(user));

        mockMvc.perform(post("/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized"));
    }

    @Test
    public void testGenerateQuizForCourse_NotFound() throws Exception {
        GenerateQuizRequestDTO requestDTO = new GenerateQuizRequestDTO();
        // Set properties of requestDTO as needed

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new ResourceNotFoundException("Course not found")).when(quizService).generateQuizForCourse(eq(requestDTO), eq(user));

        mockMvc.perform(post("/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Course not found"))
                .andExpect(jsonPath("$.errors[0]").value("Course not found"));
    }


    @Test
    public void testGetQuizGrades_Success() throws Exception {
        Long quizId = 1L;
        List<GradeDTO> grades = new ArrayList<>();
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setGrade(90.0);
        gradeDTO.setMaxGrade(100);
        grades.add(gradeDTO);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(quizService.getQuizGrades(eq(quizId), eq(user))).thenReturn(grades);

        mockMvc.perform(get("/quizzes/{quizId}/grades", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grades fetched successfully"));
    }

    @Test
    public void testGetQuizGrades_Unauthenticated() throws Exception {
        Long quizId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        mockMvc.perform(get("/quizzes/{quizId}/grades", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    public void testGetQuizGrades_InvalidUser() throws Exception {
        Long quizId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new InvalidUser("User is not authorized")).when(quizService).getQuizGrades(eq(quizId), eq(user));

        mockMvc.perform(get("/quizzes/{quizId}/grades", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized"));
    }

    @Test
    public void testGetQuizGrades_NotFound() throws Exception {
        Long quizId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new ResourceNotFoundException("Quiz not found")).when(quizService).getQuizGrades(eq(quizId), eq(user));

        mockMvc.perform(get("/quizzes/{quizId}/grades", quizId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Quiz not found"))
                .andExpect(jsonPath("$.errors[0]").value("Quiz not found"));
    }
}