package com.main.lms.controller;

import com.main.lms.dtos.*;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.services.StudentAssignmentService;
import com.main.lms.utility.SessionIdUtility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(StudentAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
public class StudentAssignmentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private StudentAssignmentService studentAssignmentService;

        @MockBean
        private SessionIdUtility sessionIdUtility;

        @Autowired
        private ObjectMapper objectMapper;

        private CustomUserDetails userDetails;

        @BeforeEach
        public void setUp() {
                userDetails = mock(CustomUserDetails.class);
                User mockUser = mock(User.class); // Mock the User object
                when(userDetails.getUser()).thenReturn(mockUser); // Stub getUser() to return the mock User
                when(mockUser.getId()).thenReturn(1L); // Stub getId() on the mock User
        }

        @Test
        public void testCreateStudentAssignment_Success() throws Exception {
                CreateStudentAssignmentDTO dto = new CreateStudentAssignmentDTO();
                // Set necessary fields in dto
                dto.setCourseId(1L);
                dto.setAssignmentId(1L);
                ;

                StudentAssignmentResponseDTO responseDTO = new StudentAssignmentResponseDTO();
                // Set necessary fields in responseDTO
                responseDTO.setId(1L);

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
                when(studentAssignmentService.createStudentAssignment(any(CreateStudentAssignmentDTO.class), eq(1L)))
                                .thenReturn(responseDTO);

                mockMvc.perform(post("/student-assignments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Student Assignment created successfully"))
                                .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        public void testCreateStudentAssignment_InternalServerError() throws Exception {
                CreateStudentAssignmentDTO dto = new CreateStudentAssignmentDTO();
                // Set necessary fields in dto

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
                when(studentAssignmentService.createStudentAssignment(any(CreateStudentAssignmentDTO.class), eq(1L)))
                                .thenThrow(new RuntimeException("Error creating assignment"));

                mockMvc.perform(post("/student-assignments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Failed to create Student Assignment"))
                                .andExpect(jsonPath("$.errors[0]").value("Error creating assignment"));
        }

        @Test
        public void testGetStudentAssignmentsByCourseId_Success() throws Exception {
                Long courseId = 1L;
                List<StudentAssignmentResponseDTO> assignments = List.of(new StudentAssignmentResponseDTO());

                when(studentAssignmentService.getStudentAssignmentsByCourseId(eq(courseId))).thenReturn(assignments);

                mockMvc.perform(get("/student-assignments/courses/{courseId}", courseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Assignments for course found"))
                                .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        public void testGetStudentAssignmentById_Success() throws Exception {
                Long assignmentId = 1L;
                StudentAssignmentResponseDTO assignment = new StudentAssignmentResponseDTO();
                assignment.setId(assignmentId);

                when(studentAssignmentService.getStudentAssignmentById(eq(assignmentId))).thenReturn(assignment);

                mockMvc.perform(get("/student-assignments/{id}", assignmentId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Student Assignment found"))
                                .andExpect(jsonPath("$.data.id").value(assignmentId));
        }

        @Test
        public void testGetStudentAssignmentsByCourseIdAndUserId_Success() throws Exception {
                Long courseId = 1L;
                StudentAssignmentResponseDTO assignment = new StudentAssignmentResponseDTO();

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
                when(studentAssignmentService.getStudentAssignmentByCourseIdAndUserId(eq(courseId), eq(1L)))
                                .thenReturn(assignment);

                mockMvc.perform(get("/student-assignments/users/me/courses/{courseId}", courseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Assignments for course found"))
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        public void testGetStudentAssignmentsByCourseIdAndUserId_NotFound() throws Exception {
                Long courseId = 1L;

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
                when(studentAssignmentService.getStudentAssignmentByCourseIdAndUserId(eq(courseId), eq(1L)))
                                .thenThrow(new NoSuchElementException("Assignment Submission not found"));

                mockMvc.perform(get("/student-assignments/users/me/courses/{courseId}", courseId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Assignment Submission not found"))
                                .andExpect(jsonPath("$.errors[0]").value("Assignment Submission not found"));
        }

        @Test
        public void testGradeStudentAssignment_Success() throws Exception {
                Long assignmentId = 1L;
                GradeAssignmentDTO dto = new GradeAssignmentDTO();
                User instructor = new User();
                instructor.setId(1L);
                instructor.setRole(UserRole.INSTRUCTOR);
                CustomUserDetails user = new CustomUserDetails(instructor);
                // Set necessary fields in dto
                dto.setGrade(95);
                dto.setFeedback("Great work!");

                StudentAssignmentResponseDTO responseDTO = new StudentAssignmentResponseDTO();
                responseDTO.setId(assignmentId);

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(user);
                when(studentAssignmentService.gradeStudentAssignmentAndAddFeedback(eq(assignmentId),
                                any(GradeAssignmentDTO.class), eq(1L)))
                                .thenReturn(responseDTO);

                mockMvc.perform(put("/student-assignments/grade/{id}", assignmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Assignment graded successfully"))
                                .andExpect(jsonPath("$.data.id").value(assignmentId));
        }

        @Test
        public void testGradeStudentAssignment_Unauthorized() throws Exception {
                Long assignmentId = 1L;
                GradeAssignmentDTO dto = new GradeAssignmentDTO();

                when(sessionIdUtility.getUserFromSessionId()).thenThrow(new ClassCastException());

                mockMvc.perform(put("/student-assignments/grade/{id}", assignmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User is not authenticated"));
        }

        @Test
        public void testGradeStudentAssignment_InvalidUser() throws Exception {
                Long assignmentId = 1L;
                GradeAssignmentDTO dto = new GradeAssignmentDTO();

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
                doThrow(new InvalidUser("User is not authorized")).when(studentAssignmentService)
                                .gradeStudentAssignmentAndAddFeedback(eq(assignmentId), any(GradeAssignmentDTO.class),
                                                eq(1L));

                mockMvc.perform(put("/student-assignments/grade/{id}", assignmentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User is not authorized"))
                                .andExpect(jsonPath("$.errors[0]").value("User is not authorized"));
        }

        @Test
        public void testGetStudentAssignmentsByUserId_Success() throws Exception {
                Long userId = 1L;
                StudentAssignmentResponseDTO assignment = new StudentAssignmentResponseDTO();
                assignment.setId(1L);

                when(studentAssignmentService.getStudentAssignmentById(eq(userId))).thenReturn(assignment);

                mockMvc.perform(get("/student-assignments/users/{userId}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Assignments for user found"))
                                .andExpect(jsonPath("$.data").isArray());
        }
}