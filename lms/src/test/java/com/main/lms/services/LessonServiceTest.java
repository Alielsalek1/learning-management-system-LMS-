package com.main.lms.services;

import com.main.lms.dtos.LessonRequestDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.Lesson;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.LessonRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LessonService lessonService;

    private User instructor;
    private Course course;
    private Lesson lesson;
    private LessonRequestDTO lessonRequestDTO;

    @BeforeEach
    public void setUp() {
        // Initialize Instructor
        instructor = new User();
        instructor.setId(1L);
        instructor.setName("Instructor Name");
        instructor.setRole(UserRole.INSTRUCTOR);

        // Initialize Course
        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setInstructor(instructor);

        // Initialize Lesson
        lesson = new Lesson();
        lesson.setLessonId(1L);
        lesson.setOtp("123456");
        lesson.setCourse(course);

        // Initialize LessonRequestDTO
        lessonRequestDTO = new LessonRequestDTO();
        lessonRequestDTO.setCourseId(course.getId());
        lessonRequestDTO.setOtp("123456");
    }

    @Test
    public void testCreateLesson_Success() {
        // Arrange
        Long userId = instructor.getId(); // The user is the instructor
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson savedLesson = invocation.getArgument(0);
            savedLesson.setLessonId(1L);
            return savedLesson;
        });

        // Act
        Lesson createdLesson = lessonService.createLesson(lessonRequestDTO, userId);

        // Assert
        assertNotNull(createdLesson);
        assertEquals(1L, createdLesson.getLessonId());
        assertEquals(course, createdLesson.getCourse());
        assertEquals("123456", createdLesson.getOtp());

        verify(courseRepository).findById(course.getId());
        verify(lessonRepository).save(any(Lesson.class));
        verify(notificationService).notifyUser(instructor.getId(), "New Lesson Created Successfully for course " + course.getTitle());
    }

    @Test
    public void testCreateLesson_CourseNotFound() {
        // Arrange
        Long userId = instructor.getId();
        Long courseId = course.getId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.createLesson(lessonRequestDTO, userId);
        });

        assertEquals("Course not found with ID: " + courseId, exception.getMessage());

        verify(courseRepository).findById(courseId);
        verify(lessonRepository, never()).save(any(Lesson.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testCreateLesson_InvalidUser() {
        // Arrange
        Long userId = 99L; // Different user ID
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () -> {
            lessonService.createLesson(lessonRequestDTO, userId);
        });

        assertEquals("You are not authorized to create a lesson for this course", exception.getMessage());

        verify(courseRepository).findById(course.getId());
        verify(lessonRepository, never()).save(any(Lesson.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testGetAllLessons() {
        // Arrange
        List<Lesson> lessons = Arrays.asList(lesson);
        when(lessonRepository.findAll()).thenReturn(lessons);

        // Act
        List<Lesson> result = lessonService.getAllLessons();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lesson, result.get(0));

        verify(lessonRepository).findAll();
    }

    @Test
    public void testGetLessonById_Success() {
        // Arrange
        Long lessonId = lesson.getLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // Act
        Lesson result = lessonService.getLessonById(lessonId);

        // Assert
        assertNotNull(result);
        assertEquals(lesson, result);

        verify(lessonRepository).findById(lessonId);
    }

    @Test
    public void testGetLessonById_NotFound() {
        // Arrange
        Long lessonId = lesson.getLessonId();
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.getLessonById(lessonId);
        });

        assertEquals("Lesson not found with ID: " + lessonId, exception.getMessage());

        verify(lessonRepository).findById(lessonId);
    }

    @Test
    public void testUpdateLesson_Success() {
        // Arrange
        Long lessonId = lesson.getLessonId();
        Long userId = instructor.getId();
        String newOtp = "654321";
        Long newCourseId = course.getId(); // Using the same course for simplicity
        LessonRequestDTO updatedLessonDTO = new LessonRequestDTO();
        updatedLessonDTO.setOtp(newOtp);
        updatedLessonDTO.setCourseId(newCourseId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(courseRepository.findById(newCourseId)).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Lesson updatedLesson = lessonService.updateLesson(lessonId, updatedLessonDTO, userId);

        // Assert
        assertNotNull(updatedLesson);
        assertEquals(lessonId, updatedLesson.getLessonId());
        assertEquals(newOtp, updatedLesson.getOtp());
        assertEquals(course, updatedLesson.getCourse());

        verify(lessonRepository).findById(lessonId);
        verify(courseRepository).findById(newCourseId);
        verify(lessonRepository).save(lesson);
        verify(notificationService).notifyUser(instructor.getId(), "Lesson Updated Successfully for course " + course.getTitle());
    }

    @Test
    public void testUpdateLesson_LessonNotFound() {
        // Arrange
        Long lessonId = lesson.getLessonId();
        Long userId = instructor.getId();
        LessonRequestDTO updatedLessonDTO = new LessonRequestDTO();
        updatedLessonDTO.setOtp("654321");
        updatedLessonDTO.setCourseId(course.getId());

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.updateLesson(lessonId, updatedLessonDTO, userId);
        });

        assertEquals("Lesson not found with ID: " + lessonId, exception.getMessage());

        verify(lessonRepository).findById(lessonId);
        verify(courseRepository, never()).findById(anyLong());
        verify(lessonRepository, never()).save(any(Lesson.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testUpdateLesson_InvalidUser() {
        // Arrange
        Long lessonId = lesson.getLessonId();
        Long userId = 99L; // Different user ID
        LessonRequestDTO updatedLessonDTO = new LessonRequestDTO();
        updatedLessonDTO.setOtp("654321");
        updatedLessonDTO.setCourseId(course.getId());

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () -> {
            lessonService.updateLesson(lessonId, updatedLessonDTO, userId);
        });

        assertEquals("You are not authorized to update a lesson for this course", exception.getMessage());

        verify(lessonRepository).findById(lessonId);
        verify(courseRepository, never()).findById(anyLong());
        verify(lessonRepository, never()).save(any(Lesson.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testUpdateLesson_CourseNotFound() {
        // Arrange
        Long lessonId = lesson.getLessonId();
        Long userId = instructor.getId();
        Long newCourseId = 99L; // Non-existent course ID
        LessonRequestDTO updatedLessonDTO = new LessonRequestDTO();
        updatedLessonDTO.setOtp("654321");
        updatedLessonDTO.setCourseId(newCourseId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(courseRepository.findById(newCourseId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.updateLesson(lessonId, updatedLessonDTO, userId);
        });

        assertEquals("Course not found with ID: " + newCourseId, exception.getMessage());

        verify(lessonRepository).findById(lessonId);
        verify(courseRepository).findById(newCourseId);
        verify(lessonRepository, never()).save(any(Lesson.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testGetLessonsByCourseId_Success() {
        // Arrange
        Long courseId = course.getId();
        List<Lesson> lessons = Arrays.asList(lesson);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourse(course)).thenReturn(lessons);

        // Act
        List<Lesson> result = lessonService.getLessonsByCourseId(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lesson, result.get(0));

        verify(courseRepository).findById(courseId);
        verify(lessonRepository).findByCourse(course);
    }

    @Test
    public void testGetLessonsByCourseId_CourseNotFound() {
        // Arrange
        Long courseId = course.getId();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lessonService.getLessonsByCourseId(courseId);
        });

        assertEquals("Course not found with ID: " + courseId, exception.getMessage());

        verify(courseRepository).findById(courseId);
        verify(lessonRepository, never()).findByCourse(any(Course.class));
    }
}