package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.EnrollmentResponseDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.services.EnrollmentService;
import com.main.lms.utility.SessionIdUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
public class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private SessionIdUtility sessionIdUtility;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(2L);
        userDetails = new CustomUserDetails(user);
    }

    @Test
    public void testCreateEnrollment_Success() throws Exception {
        Long courseId = 1L;
        EnrollmentResponseDTO enrollmentResponseDTO = new EnrollmentResponseDTO();

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(enrollmentService.addEnrollment(courseId, user.getId())).thenReturn(enrollmentResponseDTO);

        mockMvc.perform(post("/enrollments/courses/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testCreateEnrollment_UserNotLoggedIn() throws Exception {
        Long courseId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException("User is not logged in"));

        mockMvc.perform(post("/enrollments/courses/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to enroll student"))
                .andExpect(jsonPath("$.errors[0]").value("User is not logged in"));
    }

    @Test
    public void testCreateEnrollment_InternalServerError() throws Exception {
        Long courseId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(enrollmentService.addEnrollment(courseId, user.getId()))
                .thenThrow(new RuntimeException("Enrollment failed"));

        mockMvc.perform(post("/enrollments/courses/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to enroll student"))
                .andExpect(jsonPath("$.errors[0]").value("Enrollment failed"));
    }

    @Test
    public void testGetAllEnrollments_Success() throws Exception {
        EnrollmentResponseDTO enrollment1 = new EnrollmentResponseDTO();
        EnrollmentResponseDTO enrollment2 = new EnrollmentResponseDTO();
        List<EnrollmentResponseDTO> enrollments = Arrays.asList(enrollment1, enrollment2);

        when(enrollmentService.getAllEnrollments()).thenReturn(enrollments);

        mockMvc.perform(get("/enrollments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    public void testGetEnrollmentById_Success() throws Exception {
        Long enrollmentId = 1L;
        EnrollmentResponseDTO enrollmentResponseDTO = new EnrollmentResponseDTO();

        when(enrollmentService.getEnrollmentById(enrollmentId)).thenReturn(Optional.of(enrollmentResponseDTO));

        mockMvc.perform(get("/enrollments/{id}", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetEnrollmentById_NotFound() throws Exception {
        Long enrollmentId = 1L;

        when(enrollmentService.getEnrollmentById(enrollmentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/enrollments/{id}", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Enrollment not found"))
                .andExpect(jsonPath("$.errors[0]").value("Enrollment not found"));
    }

    @Test
    public void testGetEnrollmentByStudentAndCourse_Success() throws Exception {
        Long studentId = 2L;
        Long courseId = 1L;
        EnrollmentResponseDTO enrollmentResponseDTO = new EnrollmentResponseDTO();

        when(enrollmentService.getEnrollmentByStudentAndCourse(studentId, courseId))
                .thenReturn(Optional.of(enrollmentResponseDTO));

        mockMvc.perform(get("/enrollments/students/{studentId}/courses/{courseId}", studentId, courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetEnrollmentByStudentAndCourse_NotFound() throws Exception {
        Long studentId = 2L;
        Long courseId = 1L;

        when(enrollmentService.getEnrollmentByStudentAndCourse(studentId, courseId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/enrollments/students/{studentId}/courses/{courseId}", studentId, courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Note: The original controller returns 200 even if not found
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Enrollment for student and course"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testGetEnrollmentsByStudent_Success() throws Exception {
        Long studentId = 2L;
        EnrollmentResponseDTO enrollment1 = new EnrollmentResponseDTO();
        EnrollmentResponseDTO enrollment2 = new EnrollmentResponseDTO();
        List<EnrollmentResponseDTO> enrollments = Arrays.asList(enrollment1, enrollment2);

        when(enrollmentService.getEnrollmentsByStudent(studentId)).thenReturn(enrollments);

        mockMvc.perform(get("/enrollments/students/{studentId}", studentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All enrollments for student"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    public void testGetEnrollmentsByCourse_Success() throws Exception {
        Long courseId = 1L;
        EnrollmentResponseDTO enrollment1 = new EnrollmentResponseDTO();
        EnrollmentResponseDTO enrollment2 = new EnrollmentResponseDTO();
        List<EnrollmentResponseDTO> enrollments = Arrays.asList(enrollment1, enrollment2);

        when(enrollmentService.getEnrollmentsByCourse(courseId)).thenReturn(enrollments);

        mockMvc.perform(get("/enrollments/courses/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All enrollments for course"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}