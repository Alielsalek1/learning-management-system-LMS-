package com.main.lms.controller;

import com.main.lms.dtos.GradeDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.QuizService;
import com.main.lms.utility.SessionIdUtility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Import the ObjectMapper if needed
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @MockBean
    private SessionIdUtility sessionIdUtility;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);

        userDetails = new CustomUserDetails(user);
    }

    @Test
    public void testGetQuizGradesForCurrentUser_Success() throws Exception {
        List<GradeDTO> grades = List.of(new GradeDTO());

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(quizService.getStudentQuizGrades(user.getId())).thenReturn(grades);

        mockMvc.perform(get("/students/me/quiz-grades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grades fetched successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetQuizGradesForCurrentUser_Unauthenticated() throws Exception {
        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

        mockMvc.perform(get("/students/me/quiz-grades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    public void testGetQuizGradesForCurrentUser_ResourceNotFound() throws Exception {
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new ResourceNotFoundException("Grades not found"))
                .when(quizService).getStudentQuizGrades(user.getId());

        mockMvc.perform(get("/students/me/quiz-grades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Grades not found"))
                .andExpect(jsonPath("$.errors[0]").value("Grades not found"));
    }


    @Test
    public void testGetStudentQuizGrades_Success() throws Exception {
        Long studentId = 2L;
        List<GradeDTO> grades = List.of(new GradeDTO());

        when(quizService.getStudentQuizGrades(studentId)).thenReturn(grades);

        mockMvc.perform(get("/students/{studentId}/quiz-grades", studentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grades fetched successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetStudentQuizGrades_ResourceNotFound() throws Exception {
        Long studentId = 2L;
        doThrow(new ResourceNotFoundException("Grades not found"))
                .when(quizService).getStudentQuizGrades(studentId);

        mockMvc.perform(get("/students/{studentId}/quiz-grades", studentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Grades not found"))
                .andExpect(jsonPath("$.errors[0]").value("Grades not found"));
    }

    
}