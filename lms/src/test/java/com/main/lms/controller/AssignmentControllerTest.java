package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.*;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.AssignmentService;
import com.main.lms.utility.SessionIdUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AssignmentController.
 */
@WebMvcTest(AssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private AssignmentService assignmentService;

    @SuppressWarnings("removal")
    @MockBean
    private SessionIdUtility sessionIdUtility;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CustomUserDetails userDetails;
    private User instructor;

    @BeforeEach
    public void setUp() {
        // Initialize instructor and CustomUserDetails
        instructor = new User();
        instructor.setId(1L);
        instructor.setName("Instructor");
        instructor.setRole(UserRole.INSTRUCTOR);

        userDetails = new CustomUserDetails(instructor);
    }

    @Test
    @WithMockUser(username = "Instructor", roles = {"INSTRUCTOR"})
    public void testCreateAssignment_Success() throws Exception {
        // Arrange
        CreateAssignmentDTO createAssignmentDTO = new CreateAssignmentDTO();
        createAssignmentDTO.setCourseId(1L);
        createAssignmentDTO.setInstructions("Instructions");
        createAssignmentDTO.setMaxGrade(100);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(assignmentService.createAssignment(any(CreateAssignmentDTO.class), any(User.class)))
                .thenReturn(new AssignmentResponseDTO());

        // Act & Assert
        mockMvc.perform(post("/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAssignmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assignment created successfully"));

        verify(assignmentService).createAssignment(eq(createAssignmentDTO), eq(instructor));
    }

    @Test
    public void testCreateAssignment_Unauthenticated() throws Exception {
        // Arrange
        CreateAssignmentDTO createAssignmentDTO = new CreateAssignmentDTO();

        when(sessionIdUtility.getUserFromSessionId()).thenThrow(ClassCastException.class);

        // Act & Assert
        mockMvc.perform(post("/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAssignmentDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));

        verify(assignmentService, never()).createAssignment(any(), any());
    }


    @Test
    public void testGetAssignmentById_Success() throws Exception {
        // Arrange
        Long assignmentId = 1L;
        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
        assignmentResponseDTO.setId(assignmentId);
        CourseResponseDTO courseResponseDTO = new CourseResponseDTO();
        courseResponseDTO.setId(1L);
        assignmentResponseDTO.setCourse(courseResponseDTO);
        assignmentResponseDTO.setInstructions("Instructions");
        assignmentResponseDTO.setMaxGrade(100);

        when(assignmentService.getAssignmentById(assignmentId)).thenReturn(assignmentResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/assignments/{id}", assignmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(assignmentId.intValue()));

        verify(assignmentService).getAssignmentById(assignmentId);
    }

    @Test
    public void testGetAssignmentById_NotFound() throws Exception {
        // Arrange
        Long assignmentId = 1L;

        when(assignmentService.getAssignmentById(assignmentId)).thenThrow(new ResourceNotFoundException("Assignment not found"));

        // Act & Assert
        mockMvc.perform(get("/assignments/{id}", assignmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        verify(assignmentService).getAssignmentById(assignmentId);
    }

    @Test
    @WithMockUser(username = "Instructor", roles = {"INSTRUCTOR"})
    public void testUpdateAssignment_Success() throws Exception {
        // Arrange
        Long assignmentId = 1L;
        CreateAssignmentDTO updateDTO = new CreateAssignmentDTO();
        updateDTO.setCourseId(1L);
        updateDTO.setInstructions("Updated Instructions");
        updateDTO.setMaxGrade(90);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doNothing().when(assignmentService).updateAssignment(anyLong(), any(CreateAssignmentDTO.class), any(User.class));

        // Act & Assert
        mockMvc.perform(put("/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(assignmentService).updateAssignment(eq(assignmentId), eq(updateDTO), eq(instructor));
    }

    @Test
    public void testUpdateAssignment_Unauthenticated() throws Exception {
        // Arrange
        Long assignmentId = 1L;
        CreateAssignmentDTO updateDTO = new CreateAssignmentDTO();

        when(sessionIdUtility.getUserFromSessionId()).thenThrow(ClassCastException.class);

        // Act & Assert
        mockMvc.perform(put("/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        verify(assignmentService, never()).updateAssignment(anyLong(), any(), any());
    }

    @Test
    @WithMockUser(username = "Instructor", roles = {"INSTRUCTOR"})
    public void testUpdateAssignment_InvalidUser() throws Exception {
        // Arrange
        Long assignmentId = 1L;
        CreateAssignmentDTO updateDTO = new CreateAssignmentDTO();

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new InvalidUser("no")).when(assignmentService).updateAssignment(anyLong(), any(), any());

        // Act & Assert
        mockMvc.perform(put("/assignments/{id}", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(assignmentService).updateAssignment(eq(assignmentId), eq(updateDTO), eq(instructor));
    }

    @Test
    @WithMockUser(username = "Instructor", roles = {"INSTRUCTOR"})
    public void testDeleteAssignment_Success() throws Exception {
        // Arrange
        Long assignmentId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doNothing().when(assignmentService).deleteAssignment(anyLong(), any(User.class));

        // Act & Assert
        mockMvc.perform(delete("/assignments/{id}", assignmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(assignmentService).deleteAssignment(eq(assignmentId), eq(instructor));
    }

    @Test
    public void testDeleteAssignment_Unauthenticated() throws Exception {
        // Arrange
        Long assignmentId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenThrow(ClassCastException.class);

        // Act & Assert
        mockMvc.perform(delete("/assignments/{id}", assignmentId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        verify(assignmentService, never()).deleteAssignment(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "Instructor", roles = {"INSTRUCTOR"})
    public void testDeleteAssignment_InvalidUser() throws Exception {
        // Arrange
        Long assignmentId = 1L;

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new InvalidUser("no")).when(assignmentService).deleteAssignment(anyLong(), any());

        // Act & Assert
        mockMvc.perform(delete("/assignments/{id}", assignmentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(assignmentService).deleteAssignment(eq(assignmentId), eq(instructor));
    }


}