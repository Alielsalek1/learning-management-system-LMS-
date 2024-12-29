package com.main.lms.services;

import com.main.lms.dtos.AssignmentResponseDTO;
import com.main.lms.dtos.CreateAssignmentDTO;
import com.main.lms.entities.*;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.AssignmentRepository;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.repositories.EnrolledCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EnrolledCourseRepository enrolledCourseRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private User instructor;
    private User student;
    private Course course;
    private Assignment assignment;
    private EnrolledCourse enrolledCourse;
    private CreateAssignmentDTO createAssignmentDTO;

    @BeforeEach
    public void setUp() {
        // Initialize instructor
        instructor = new User();
        instructor.setId(1L);
        instructor.setName("Instructor Name");
        instructor.setRole(UserRole.INSTRUCTOR);

        // Initialize student
        student = new User();
        student.setId(2L);
        student.setName("Student Name");
        student.setRole(UserRole.STUDENT);

        // Initialize course
        course = new Course();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setInstructor(instructor);

        // Initialize assignment
        assignment = new Assignment();
        assignment.setAssignmentId(1L);
        assignment.setCourse(course);
        assignment.setInstructions("Assignment Instructions");
        assignment.setMaxGrade(100);

        // Initialize enrolled course
        enrolledCourse = new EnrolledCourse();
        enrolledCourse.setCourse(course);
        enrolledCourse.setStudent(student);

        // Initialize createAssignmentDTO
        createAssignmentDTO = new CreateAssignmentDTO();
        createAssignmentDTO.setCourseId(course.getId());
        createAssignmentDTO.setInstructions("Assignment Instructions");
        createAssignmentDTO.setMaxGrade(100);
    }

    @Test
    public void testCreateAssignment_Success() {
        // Arrange
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment savedAssignment = invocation.getArgument(0);
            savedAssignment.setAssignmentId(1L);
            return savedAssignment;
        });
        when(enrolledCourseRepository.findByCourse(course)).thenReturn(Arrays.asList(enrolledCourse));

        // Act
        AssignmentResponseDTO responseDTO = assignmentService.createAssignment(createAssignmentDTO, instructor);

        // Assert
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());
        assertEquals(course.getId(), responseDTO.getCourse().getId());
        assertEquals("Assignment Instructions", responseDTO.getInstructions());
        assertEquals(100.0, responseDTO.getMaxGrade());

        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository).save(any(Assignment.class));
        verify(enrolledCourseRepository).findByCourse(course);
        verify(notificationService).notifyUser(instructor.getId(),
                "New Assignment Created Successfully for course " + course.getTitle());
        verify(notificationService).notifyUser(student.getId(),
                "New Assignment has been posted for course " + course.getTitle());
    }

    @Test
    public void testCreateAssignment_CourseNotFound() {
        // Arrange
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                assignmentService.createAssignment(createAssignmentDTO, instructor));

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(enrolledCourseRepository, never()).findByCourse(any(Course.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testCreateAssignment_InvalidUser() {
        // Arrange
        User anotherInstructor = new User();
        anotherInstructor.setId(99L);
        anotherInstructor.setName("Another Instructor");
        anotherInstructor.setRole(UserRole.INSTRUCTOR);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                assignmentService.createAssignment(createAssignmentDTO, anotherInstructor));

        assertEquals("You are not the instructor for this course", exception.getMessage());

        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(enrolledCourseRepository, never()).findByCourse(any(Course.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testGetAssignmentById_Success() {
        // Arrange
        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));

        // Act
        AssignmentResponseDTO responseDTO = assignmentService.getAssignmentById(assignment.getAssignmentId());

        // Assert
        assertNotNull(responseDTO);
        assertEquals(assignment.getAssignmentId(), responseDTO.getId());
        assertEquals(assignment.getCourse().getId(), responseDTO.getCourse().getId());
        assertEquals(assignment.getInstructions(), responseDTO.getInstructions());
        assertEquals(assignment.getMaxGrade(), responseDTO.getMaxGrade());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
    }

    @Test
    public void testGetAssignmentById_NotFound() {
        // Arrange
        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                assignmentService.getAssignmentById(assignment.getAssignmentId()));

        assertEquals("Assignment not found with ID: " + assignment.getAssignmentId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
    }

    @Test
    public void testUpdateAssignment_Success() {
        // Arrange
        CreateAssignmentDTO updateDTO = new CreateAssignmentDTO();
        updateDTO.setCourseId(course.getId());
        updateDTO.setInstructions("Updated Instructions");
        updateDTO.setMaxGrade(90);

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(enrolledCourseRepository.findByCourse(course)).thenReturn(Arrays.asList(enrolledCourse));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assignmentService.updateAssignment(assignment.getAssignmentId(), updateDTO, instructor);

        // Assert
        assertEquals("Updated Instructions", assignment.getInstructions());
        assertEquals(90.0, assignment.getMaxGrade());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(enrolledCourseRepository).findByCourse(course);
        verify(assignmentRepository).save(assignment);
        verify(notificationService).notifyUser(instructor.getId(),
                "Assignment Updated Successfully for course " + course.getTitle());
        verify(notificationService).notifyUser(student.getId(),
                "Assignment Updated Successfully for course " + course.getTitle());
    }

    @Test
    public void testUpdateAssignment_AssignmentNotFound() {
        // Arrange
        CreateAssignmentDTO updateDTO = new CreateAssignmentDTO();
        updateDTO.setCourseId(course.getId());
        updateDTO.setInstructions("Updated Instructions");
        updateDTO.setMaxGrade(90);

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                assignmentService.updateAssignment(assignment.getAssignmentId(), updateDTO, instructor));

        assertEquals("Assignment not found with ID: " + assignment.getAssignmentId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository, never()).findById(anyLong());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testUpdateAssignment_CourseNotFound() {
        // Arrange
        CreateAssignmentDTO updateDTO = new CreateAssignmentDTO();
        updateDTO.setCourseId(99L); // Non-existent course ID
        updateDTO.setInstructions("Updated Instructions");
        updateDTO.setMaxGrade(90);

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(updateDTO.getCourseId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                assignmentService.updateAssignment(assignment.getAssignmentId(), updateDTO, instructor));

        assertEquals("Course not found with ID: " + updateDTO.getCourseId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(updateDTO.getCourseId());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testUpdateAssignment_InvalidUser() {
        // Arrange
        User anotherInstructor = new User();
        anotherInstructor.setId(99L);
        anotherInstructor.setName("Another Instructor");
        anotherInstructor.setRole(UserRole.INSTRUCTOR);

        CreateAssignmentDTO updateDTO = new CreateAssignmentDTO();
        updateDTO.setCourseId(course.getId());
        updateDTO.setInstructions("Updated Instructions");
        updateDTO.setMaxGrade(90);

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                assignmentService.updateAssignment(assignment.getAssignmentId(), updateDTO, anotherInstructor));

        assertEquals("You are not the instructor for this course", exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository, never()).save(any(Assignment.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testDeleteAssignment_Success() {
        // Arrange
        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        // Act
        assignmentService.deleteAssignment(assignment.getAssignmentId(), instructor);

        // Assert
        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository).delete(assignment);
        verify(notificationService).notifyUser(instructor.getId(),
                "Assignment Deleted Successfully for course " + course.getTitle());
    }

    @Test
    public void testDeleteAssignment_AssignmentNotFound() {
        // Arrange
        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                assignmentService.deleteAssignment(assignment.getAssignmentId(), instructor));

        assertEquals("Assignment not found with ID: " + assignment.getAssignmentId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository, never()).findById(anyLong());
        verify(assignmentRepository, never()).delete(any(Assignment.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testDeleteAssignment_CourseNotFound() {
        // Arrange
        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                assignmentService.deleteAssignment(assignment.getAssignmentId(), instructor));

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository, never()).delete(any(Assignment.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testDeleteAssignment_InvalidUser() {
        // Arrange
        User anotherInstructor = new User();
        anotherInstructor.setId(99L);
        anotherInstructor.setName("Another Instructor");
        anotherInstructor.setRole(UserRole.INSTRUCTOR);

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                assignmentService.deleteAssignment(assignment.getAssignmentId(), anotherInstructor));

        assertEquals("You are not the instructor for this course", exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository, never()).delete(any(Assignment.class));
        verify(notificationService, never()).notifyUser(anyLong(), anyString());
    }

    @Test
    public void testGetAssignmentByCourseId_Success() {
        // Arrange
        List<Assignment> assignments = Arrays.asList(assignment);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(assignmentRepository.findByCourse(course)).thenReturn(assignments);

        // Act
        List<AssignmentResponseDTO> responseDTOs = assignmentService.getAssignmentbyCourseId(course.getId());

        // Assert
        assertNotNull(responseDTOs);
        assertEquals(1, responseDTOs.size());
        AssignmentResponseDTO responseDTO = responseDTOs.get(0);
        assertEquals(assignment.getAssignmentId(), responseDTO.getId());
        assertEquals(assignment.getCourse().getId(), responseDTO.getCourse().getId());
        assertEquals(assignment.getInstructions(), responseDTO.getInstructions());
        assertEquals(assignment.getMaxGrade(), responseDTO.getMaxGrade());

        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository).findByCourse(course);
    }

    @Test
    public void testGetAssignmentByCourseId_CourseNotFound() {
        // Arrange
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                assignmentService.getAssignmentbyCourseId(course.getId()));

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(courseRepository).findById(course.getId());
        verify(assignmentRepository, never()).findByCourse(any(Course.class));
    }
}