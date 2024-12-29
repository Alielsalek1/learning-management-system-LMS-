package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.*;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.services.CourseService;
import com.main.lms.utility.SessionIdUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@WebMvcTest(CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CourseControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CourseService courseService;
        @MockBean
        private SessionIdUtility sessionIdUtility;

        private ObjectMapper objectMapper = new ObjectMapper();

        private User instructorUser;
        private CustomUserDetails instructorDetails;

        @BeforeEach
        public void setUp() {
                // Initialize instructor user and custom user details
                instructorUser = new User();
                instructorUser.setId(1L);
                instructorUser.setName("Instructor");
                instructorUser.setRole(UserRole.INSTRUCTOR);

                instructorDetails = new CustomUserDetails(instructorUser);
        }

        @Test
        public void testCreateMaterial_CourseNotFound() throws Exception {
                // Arrange
                Long courseId = 1L;

                MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain",
                                "Test File".getBytes());

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(instructorDetails);
                doThrow(new CourseNotFoundException("Course not found"))
                                .when(courseService).addMaterials(eq(courseId), eq(instructorUser.getId()), anyList());

                // Act & Assert
                mockMvc.perform(multipart("/courses/{id}/material", courseId)
                                .file(file))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(courseService).addMaterials(eq(courseId), eq(instructorUser.getId()), anyList());
        }

        @Test
        public void testDownloadMaterialsZip_CourseNotFound() throws Exception {
                // Arrange
                Long courseId = 1L;

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(instructorDetails);
                when(courseService.getCourseMaterials(eq(courseId), eq(instructorUser.getId())))
                                .thenThrow(new CourseNotFoundException("Course not found"));

                // Act & Assert
                mockMvc.perform(get("/courses/{id}/material", courseId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(courseService).getCourseMaterials(eq(courseId), eq(instructorUser.getId()));
                verify(courseService, never()).downloadMaterialsZip(anyList(), anyLong());
        }

        @Test
        @WithMockUser(username = "Instructor", roles = { "INSTRUCTOR" })
        public void testCreateCourse_Success() throws Exception {
                // Arrange
                CourseRequestDTO courseRequestDTO = new CourseRequestDTO();
                courseRequestDTO.setTitle("New Course");
                courseRequestDTO.setDescription("Course Description");
                courseRequestDTO.setDuration("10");

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(instructorDetails);

                CourseResponseDTO courseResponseDTO = new CourseResponseDTO();
                courseResponseDTO.setId(1L);
                courseResponseDTO.setTitle("New Course");

                when(courseService.addCourse(anyString(), eq("10"), anyString(), eq(instructorUser.getId())))
                                .thenReturn(courseResponseDTO);

                // Act & Assert
                mockMvc.perform(post("/courses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(courseRequestDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("New Course"));

                verify(courseService).addCourse(eq("New Course"), eq("10"), eq("Course Description"),
                                eq(instructorUser.getId()));
        }

        

        @Test
        public void testGetCourseById_Success() throws Exception {
                // Arrange
                Long courseId = 1L;
                CourseResponseDTO courseResponseDTO = new CourseResponseDTO();
                courseResponseDTO.setId(courseId);
                courseResponseDTO.setTitle("Test Course");

                when(courseService.getCourseById(courseId)).thenReturn(courseResponseDTO);

                // Act & Assert
                mockMvc.perform(get("/courses/{id}", courseId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("Test Course"));

                verify(courseService).getCourseById(courseId);
        }

        @Test
        public void testGetCourseById_CourseNotFound() throws Exception {
                // Arrange
                Long courseId = 1L;

                when(courseService.getCourseById(courseId)).thenThrow(new CourseNotFoundException("Course not found"));

                // Act & Assert
                mockMvc.perform(get("/courses/{id}", courseId))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(courseService).getCourseById(courseId);
        }

        @Test
        @WithMockUser(username = "Instructor", roles = { "INSTRUCTOR" })
        public void testUpdateCourse_Success() throws Exception {
                // Arrange
                Long courseId = 1L;
                CourseRequestDTO courseRequestDTO = new CourseRequestDTO();
                courseRequestDTO.setTitle("Updated Course");
                courseRequestDTO.setDescription("Updated Description");
                courseRequestDTO.setDuration("20");

                CourseResponseDTO courseResponseDTO = new CourseResponseDTO();
                courseResponseDTO.setId(courseId);
                courseResponseDTO.setTitle("Updated Course");

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(instructorDetails);
                when(courseService.updateCourse(eq(courseId), any(CourseRequestDTO.class), eq(instructorUser.getId())))
                                .thenReturn(courseResponseDTO);

                // Act & Assert
                mockMvc.perform(put("/courses/{id}", courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(courseRequestDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.title").value("Updated Course"));

                verify(courseService).updateCourse(eq(courseId), any(CourseRequestDTO.class),
                                eq(instructorUser.getId()));
        }

        @Test
        @WithMockUser(username = "Instructor", roles = { "INSTRUCTOR" })
        public void testUpdateCourse_InvalidUser() throws Exception {
                // Arrange
                Long courseId = 1L;
                CourseRequestDTO courseRequestDTO = new CourseRequestDTO();
                courseRequestDTO.setTitle("Updated Course");
                courseRequestDTO.setDescription("Updated Description");
                courseRequestDTO.setDuration("20");

                when(sessionIdUtility.getUserFromSessionId()).thenReturn(instructorDetails);
                when(courseService.updateCourse(eq(courseId), any(CourseRequestDTO.class), eq(instructorUser.getId())))
                                .thenThrow(new InvalidUser("You are not the instructor of this course"));

                // Act & Assert
                mockMvc.perform(put("/courses/{id}", courseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(courseRequestDTO)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false));

                verify(courseService).updateCourse(eq(courseId), any(CourseRequestDTO.class),
                                eq(instructorUser.getId()));
        }
}