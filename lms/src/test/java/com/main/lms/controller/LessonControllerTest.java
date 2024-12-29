package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.LessonRequestDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.Lesson;
import com.main.lms.entities.User;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.LessonService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
public class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

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
        userDetails = new CustomUserDetails(user);
    }

    @Test
    public void testCreateLesson_Success() throws Exception {
        // Create a LessonRequestDTO with necessary fields
        LessonRequestDTO lessonRequestDTO = new LessonRequestDTO();
        lessonRequestDTO.setCourseId(1L); // Set required course ID
        lessonRequestDTO.setOtp("123456"); // Set required OTP or other fields

        // Create a lesson object to return from the mocked service
        Lesson lesson = new Lesson();
        lesson.setLessonId(1L);

        // Mock the behavior of sessionIdUtility and lessonService
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(lessonService.createLesson(any(LessonRequestDTO.class), eq(user.getId()))).thenReturn(lesson);

        // Perform the POST request and verify the response
        mockMvc.perform(post("/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonRequestDTO)));
    }

    @Test
    public void testCreateLesson_InvalidUser() throws Exception {
        LessonRequestDTO lessonRequestDTO = new LessonRequestDTO();
        lessonRequestDTO.setCourseId(1L);
        lessonRequestDTO.setOtp("1234");

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new InvalidUser("Invalid user")).when(lessonService).createLesson(any(LessonRequestDTO.class),
                eq(user.getId()));

        mockMvc.perform(post("/lessons"));
    }

    @Test
    public void testCreateLesson_RuntimeException() throws Exception {
        LessonRequestDTO lessonRequestDTO = new LessonRequestDTO();
        lessonRequestDTO.setCourseId(1L);
        lessonRequestDTO.setOtp("1234");

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new RuntimeException("Creation failed")).when(lessonService).createLesson(any(LessonRequestDTO.class),
                eq(user.getId()));

        mockMvc.perform(post("/lessons"));
    }

    @Test
    public void testCreateLesson_InternalServerError() throws Exception {
        LessonRequestDTO lessonRequestDTO = new LessonRequestDTO();
        lessonRequestDTO.setCourseId(1L);
        lessonRequestDTO.setOtp("1234");

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);

        mockMvc.perform(post("/lessons"));
    }

    @Test
    public void testGetLessonsByCourseId_Success() throws Exception {
        Long courseId = 1L;
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");
        Lesson lesson1 = new Lesson();
        lesson1.setLessonId(1L);
        lesson1.setCourse(course);
        Lesson lesson2 = new Lesson();
        lesson2.setLessonId(2L);
        lesson2.setCourse(course);
        List<Lesson> lessons = Arrays.asList(lesson1, lesson2);

        when(lessonService.getLessonsByCourseId(courseId)).thenReturn(lessons);

        mockMvc.perform(get("/lessons/courses/{courseId}", courseId))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLessonsByCourseId_EmptyList() throws Exception {
        Long courseId = 1L;
        when(lessonService.getLessonsByCourseId(courseId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/lessons/courses/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetLessonById_Success() throws Exception {
        Long lessonId = 1L;
        Lesson lesson = new Lesson();
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Course Title");
        lesson.setLessonId(lessonId);
        lesson.setCourse(course);

        when(lessonService.getLessonById(lessonId)).thenReturn(lesson);

        mockMvc.perform(get("/lessons/{id}", lessonId))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateLesson_Success() throws Exception {
        Long lessonId = 1L;
        LessonRequestDTO lessonRequestDTO = new LessonRequestDTO();
        // Set necessary fields in lessonRequestDTO
        Lesson updatedLesson = new Lesson();
        updatedLesson.setLessonId(lessonId);

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        when(lessonService.updateLesson(eq(lessonId), any(LessonRequestDTO.class), eq(user.getId())))
                .thenReturn(updatedLesson);

        mockMvc.perform(put("/lessons/{id}", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lesson updated successfully"))
                .andExpect(jsonPath("$.data.lessonId").value(lessonId));
    }



    @Test
    public void testUpdateLesson_NotFound() throws Exception {
        Long lessonId = 1L;
        LessonRequestDTO lessonRequestDTO = new LessonRequestDTO();
        // Set necessary fields in lessonRequestDTO

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new ResourceNotFoundException("Lesson not found")).when(lessonService).updateLesson(eq(lessonId),
                any(LessonRequestDTO.class), eq(user.getId()));

        mockMvc.perform(put("/lessons/{id}", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Lesson not found"))
                .andExpect(jsonPath("$.errors[0]").value("Lesson not found"));
    }

    @Test
    public void testUpdateLesson_RuntimeException() throws Exception {
        Long lessonId = 1L;
        LessonRequestDTO lessonRequestDTO = new LessonRequestDTO();
        // Set necessary fields in lessonRequestDTO

        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);
        doThrow(new RuntimeException("Update failed")).when(lessonService).updateLesson(eq(lessonId),
                any(LessonRequestDTO.class), eq(user.getId()));

        mockMvc.perform(put("/lessons/{id}", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lessonRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Update failed"))
                .andExpect(jsonPath("$.errors[0]").value("Update failed"));
    }
}