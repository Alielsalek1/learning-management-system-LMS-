package com.main.lms.services;


import com.main.lms.dtos.EnrollmentResponseDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.EnrolledCourse;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.EnrolledCourseRepository;
import com.main.lms.repositories.UserRepository;
import com.main.lms.services.EnrollmentService;
import com.main.lms.services.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrolledCourseRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User student;
    private User instructor;
    private Course course;
    private EnrolledCourse enrolledCourse;

    @BeforeEach
    public void setUp() {
        student = new User();
        student.setId(1L);
        student.setName("Student");
        student.setRole(UserRole.STUDENT);

        instructor = new User();
        instructor.setId(2L);
        instructor.setName("Instructor");
        instructor.setRole(UserRole.INSTRUCTOR);

        course = new Course();
        course.setId(1L);
        course.setTitle("Course Title");
        course.setInstructor(instructor);

        enrolledCourse = new EnrolledCourse();
        enrolledCourse.setId(1L);
        enrolledCourse.setStudent(student);
        enrolledCourse.setCourse(course);
        enrolledCourse.setIsConfirmed(true);
    }

    @Test
    public void testGetAllEnrollments() {
        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(enrolledCourse));

        List<EnrollmentResponseDTO> result = enrollmentService.getAllEnrollments();

        assertNotNull(result);
        assertEquals(1, result.size());
        EnrollmentResponseDTO dto = result.get(0);
        assertEquals(student.getId(), dto.getStudentId());
        assertEquals(course.getId(), dto.getCourseId());

        verify(enrollmentRepository, times(1)).findAll();
    }

    @Test
    public void testGetEnrollmentById_Found() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrolledCourse));

        Optional<EnrollmentResponseDTO> result = enrollmentService.getEnrollmentById(1L);

        assertTrue(result.isPresent());
        EnrollmentResponseDTO dto = result.get();
        assertEquals(student.getId(), dto.getStudentId());
        assertEquals(course.getId(), dto.getCourseId());

        verify(enrollmentRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEnrollmentById_NotFound() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CourseNotFoundException.class, () -> enrollmentService.getEnrollmentById(1L));

        verify(enrollmentRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetEnrollmentByStudentAndCourse_Found() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentAndCourse(student, course)).thenReturn(Arrays.asList(enrolledCourse));

        Optional<EnrollmentResponseDTO> result = enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), course.getId());

        assertTrue(result.isPresent());
        EnrollmentResponseDTO dto = result.get();
        assertEquals(student.getId(), dto.getStudentId());
        assertEquals(course.getId(), dto.getCourseId());

        verify(enrollmentRepository, times(1)).findByStudentAndCourse(student, course);
    }

    @Test
    public void testGetEnrollmentByStudentAndCourse_NotFound() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentAndCourse(student, course)).thenReturn(Collections.emptyList());

        assertThrows(CourseNotFoundException.class, () -> {
            enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), course.getId());
        });

        verify(enrollmentRepository, times(1)).findByStudentAndCourse(student, course);
    }

    @Test
    public void testGetEnrollmentsByStudent() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudent(student)).thenReturn(Arrays.asList(enrolledCourse));

        List<EnrollmentResponseDTO> result = enrollmentService.getEnrollmentsByStudent(student.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        EnrollmentResponseDTO dto = result.get(0);
        assertEquals(student.getId(), dto.getStudentId());
        assertEquals(course.getId(), dto.getCourseId());

        verify(enrollmentRepository, times(1)).findByStudent(student);
    }

    @Test
    public void testGetEnrollmentsByCourse() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourse(course)).thenReturn(Arrays.asList(enrolledCourse));

        List<EnrollmentResponseDTO> result = enrollmentService.getEnrollmentsByCourse(course.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        EnrollmentResponseDTO dto = result.get(0);
        assertEquals(student.getId(), dto.getStudentId());
        assertEquals(course.getId(), dto.getCourseId());

        verify(enrollmentRepository, times(1)).findByCourse(course);
    }

    @Test
    public void testAddEnrollment_Success() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentAndCourse(student, course)).thenReturn(Collections.emptyList());
        when(enrollmentRepository.save(any(EnrolledCourse.class))).thenReturn(enrolledCourse);

        EnrollmentResponseDTO result = enrollmentService.addEnrollment(course.getId(), student.getId());

        assertNotNull(result);
        assertEquals(student.getId(), result.getStudentId());
        assertEquals(course.getId(), result.getCourseId());

        verify(courseRepository, times(1)).findById(course.getId());
        verify(userRepository, times(1)).findById(student.getId());
        verify(enrollmentRepository, times(1)).findByStudentAndCourse(student, course);
        verify(enrollmentRepository, times(1)).save(any(EnrolledCourse.class));
        verify(notificationService, times(1)).notifyUser(student.getId(),
                "You have been enrolled in a the following course: " + course.getTitle());
        verify(notificationService, times(1)).notifyUser(instructor.getId(),
                "A new student has been enrolled in your course: " + course.getTitle());
    }

    @Test
    public void testAddEnrollment_CourseOrStudentNotFound() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(student.getId())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.addEnrollment(course.getId(), student.getId());
        });
        assertEquals("Entity Not Found", exception.getMessage());

        verify(courseRepository, times(1)).findById(course.getId());
        verify(userRepository, times(1)).findById(student.getId());
        verify(enrollmentRepository, never()).save(any(EnrolledCourse.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testAddEnrollment_UserNotStudent() {
        User nonStudentUser = new User();
        nonStudentUser.setId(3L);
        nonStudentUser.setRole(UserRole.INSTRUCTOR);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.findById(nonStudentUser.getId())).thenReturn(Optional.of(nonStudentUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.addEnrollment(course.getId(), nonStudentUser.getId());
        });
        assertEquals("Only Students Can Be Enrolled in Courses", exception.getMessage());

        verify(courseRepository, times(1)).findById(course.getId());
        verify(userRepository, times(1)).findById(nonStudentUser.getId());
        verify(enrollmentRepository, never()).save(any(EnrolledCourse.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testAddEnrollment_StudentAlreadyEnrolled() {
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentAndCourse(student, course)).thenReturn(Arrays.asList(enrolledCourse));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            enrollmentService.addEnrollment(course.getId(), student.getId());
        });
        assertEquals("Student is already enrolled in this course", exception.getMessage());

        verify(courseRepository, times(1)).findById(course.getId());
        verify(userRepository, times(1)).findById(student.getId());
        verify(enrollmentRepository, times(1)).findByStudentAndCourse(student, course);
        verify(enrollmentRepository, never()).save(any(EnrolledCourse.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

}