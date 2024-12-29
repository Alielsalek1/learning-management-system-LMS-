package com.main.lms.services;

import com.main.lms.dtos.CourseResponseDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.UserRepository;
import com.main.lms.services.CourseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private User instructor;

    @BeforeEach
    public void setUp() {
        instructor = new User();
        instructor.setId(1L);
        instructor.setName("John Doe");
        instructor.setRole(UserRole.INSTRUCTOR);

        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setDescription("Course Description");
        course.setDuration("10 weeks");
        course.setInstructor(instructor);
        course.setMaterials(new ArrayList<>());
    }

    @Test
    public void testGetAllCourses() {
        // Arrange
        List<Course> courses = Arrays.asList(course);
        when(courseRepository.findAll()).thenReturn(courses);

        // Act
        List<CourseResponseDTO> result = courseService.getAllCourses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(course.getTitle(), result.get(0).getTitle());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    public void testGetCourseById_Success() throws CourseNotFoundException {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // Act
        CourseResponseDTO result = courseService.getCourseById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(course.getTitle(), result.getTitle());
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetCourseById_CourseNotFound() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CourseNotFoundException.class, () -> {
            courseService.getCourseById(1L);
        });
        verify(courseRepository, times(1)).findById(1L);
    }

    @Test
    public void testAddCourse_Success() {
        // Arrange
        when(userRepository.findById(instructor.getId())).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setId(1L);
            return savedCourse;
        });

        // Act
        CourseResponseDTO result = courseService.addCourse(
                course.getTitle(),
                course.getDuration(),
                course.getDescription(),
                instructor.getId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(course.getTitle(), result.getTitle());
        verify(userRepository, times(1)).findById(instructor.getId());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    public void testAddCourse_InstructorNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.addCourse("Course Title", "10 weeks", "Description", 1L);
        });
        assertEquals("Instructor Not Found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    public void testAddCourse_UserNotInstructor() {
        // Arrange
        User student = new User();
        student.setId(2L);
        student.setRole(UserRole.STUDENT);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.addCourse("Course Title", "10 weeks", "Description", 2L);
        });
        assertEquals("Only Instructors Can Be Added to Course Entity", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    public void testDeleteCourseById_Success() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseRepository).deleteById(1L);

        // Act
        courseService.deleteCourseById(1L, instructor.getId());

        // Assert
        verify(courseRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).deleteById(1L);
    }


    @Test
    public void testDownloadMaterialsZip_Failure() {
        // Arrange
        Path path = Paths.get("nonexistentfile.txt");
        List<Path> paths = Collections.singletonList(path);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseService.downloadMaterialsZip(paths, 1L);
        });
        assertEquals("Failed to create zip file", exception.getMessage());
    }
}