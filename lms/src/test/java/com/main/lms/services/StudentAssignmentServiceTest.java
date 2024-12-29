package com.main.lms.services;

import com.main.lms.dtos.CreateStudentAssignmentDTO;
import com.main.lms.dtos.EnrollmentResponseDTO;
import com.main.lms.dtos.GradeAssignmentDTO;
import com.main.lms.dtos.StudentAssignmentResponseDTO;
import com.main.lms.entities.*;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentAssignmentServiceTest {

    @Mock
    private StudentAssignmentRepository studentAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private StudentAssignmentService studentAssignmentService;

    private User student;
    private User instructor;
    private Course course;
    private Assignment assignment;
    private StudentAssignment studentAssignment;

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
        assignment.setInstructions("Test Assignment");

        // Initialize student assignment
        studentAssignment = new StudentAssignment();
        studentAssignment.setId(1L);
        studentAssignment.setAssignment(assignment);
        studentAssignment.setCourse(course);
        studentAssignment.setStudent(student);
        studentAssignment.setFileNames(new ArrayList<>());
    }

    @Test
    public void testCreateStudentAssignment_Success() {
        // Arrange
        CreateStudentAssignmentDTO dto = new CreateStudentAssignmentDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setCourseId(course.getId());

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), course.getId())).thenReturn(Optional.of(new EnrollmentResponseDTO()));
        when(studentAssignmentRepository.save(any(StudentAssignment.class))).thenAnswer(invocation -> {
            StudentAssignment savedAssignment = invocation.getArgument(0);
            savedAssignment.setId(1L);
            return savedAssignment;
        });

        // Act
        StudentAssignmentResponseDTO responseDTO = studentAssignmentService.createStudentAssignment(dto, student.getId());

        // Assert
        assertNotNull(responseDTO);
        assertEquals(1L, responseDTO.getId());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(userRepository).findById(student.getId());
        verify(enrollmentService).getEnrollmentByStudentAndCourse(student.getId(), course.getId());
        verify(studentAssignmentRepository).save(any(StudentAssignment.class));
    }

    @Test
    public void testCreateStudentAssignment_AssignmentNotFound() {
        // Arrange
        CreateStudentAssignmentDTO dto = new CreateStudentAssignmentDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setCourseId(course.getId());

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                studentAssignmentService.createStudentAssignment(dto, student.getId()));

        assertEquals("Assignment not found with ID: " + assignment.getAssignmentId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(enrollmentService, never()).getEnrollmentByStudentAndCourse(anyLong(), anyLong());
        verify(studentAssignmentRepository, never()).save(any(StudentAssignment.class));
    }

    @Test
    public void testCreateStudentAssignment_CourseNotFound() {
        // Arrange
        CreateStudentAssignmentDTO dto = new CreateStudentAssignmentDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setCourseId(course.getId());

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                studentAssignmentService.createStudentAssignment(dto, student.getId()));

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(userRepository, never()).findById(anyLong());
        verify(enrollmentService, never()).getEnrollmentByStudentAndCourse(anyLong(), anyLong());
        verify(studentAssignmentRepository, never()).save(any(StudentAssignment.class));
    }

    @Test
    public void testCreateStudentAssignment_StudentNotFound() {
        // Arrange
        CreateStudentAssignmentDTO dto = new CreateStudentAssignmentDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setCourseId(course.getId());

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.findById(student.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                studentAssignmentService.createStudentAssignment(dto, student.getId()));

        assertEquals("Student not found with ID: " + student.getId(), exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(userRepository).findById(student.getId());
        verify(enrollmentService, never()).getEnrollmentByStudentAndCourse(anyLong(), anyLong());
        verify(studentAssignmentRepository, never()).save(any(StudentAssignment.class));
    }

    @Test
    public void testCreateStudentAssignment_StudentNotEnrolled() {
        // Arrange
        CreateStudentAssignmentDTO dto = new CreateStudentAssignmentDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setCourseId(course.getId());

        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Optional.of(assignment));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), course.getId())).thenReturn(Optional.empty());

        // Act & Assert
        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class, () ->
                studentAssignmentService.createStudentAssignment(dto, student.getId()));

        assertEquals("Student is not enrolled in this course", exception.getMessage());

        verify(assignmentRepository).findById(assignment.getAssignmentId());
        verify(courseRepository).findById(course.getId());
        verify(userRepository).findById(student.getId());
        verify(enrollmentService).getEnrollmentByStudentAndCourse(student.getId(), course.getId());
        verify(studentAssignmentRepository, never()).save(any(StudentAssignment.class));
    }

    @Test
    public void testGetAllStudentAssignments() {
        // Arrange
        List<StudentAssignment> assignments = Arrays.asList(studentAssignment);
        when(studentAssignmentRepository.findAll()).thenReturn(assignments);

        // Act
        List<StudentAssignmentResponseDTO> responseDTOs = studentAssignmentService.getAllStudentAssignments();

        // Assert
        assertNotNull(responseDTOs);
        assertEquals(1, responseDTOs.size());
        assertEquals(studentAssignment.getId(), responseDTOs.get(0).getId());

        verify(studentAssignmentRepository).findAll();
    }

    @Test
    public void testGetStudentAssignmentsByCourseId_Success() {
        // Arrange
        List<StudentAssignment> assignments = Arrays.asList(studentAssignment);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(studentAssignmentRepository.findByCourse(course)).thenReturn(assignments);

        // Act
        List<StudentAssignmentResponseDTO> responseDTOs = studentAssignmentService.getStudentAssignmentsByCourseId(course.getId());

        // Assert
        assertNotNull(responseDTOs);
        assertEquals(1, responseDTOs.size());
        assertEquals(studentAssignment.getId(), responseDTOs.get(0).getId());

        verify(courseRepository).findById(course.getId());
        verify(studentAssignmentRepository).findByCourse(course);
    }

    @Test
    public void testGetStudentAssignmentsByCourseId_CourseNotFound() {
        // Arrange
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                studentAssignmentService.getStudentAssignmentsByCourseId(course.getId()));

        assertEquals("Course not found with ID: " + course.getId(), exception.getMessage());

        verify(courseRepository).findById(course.getId());
        verify(studentAssignmentRepository, never()).findByCourse(any(Course.class));
    }

    @Test
    public void testGetStudentAssignmentById_Success() {
        // Arrange
        when(studentAssignmentRepository.findById(studentAssignment.getId())).thenReturn(Optional.of(studentAssignment));

        // Act
        StudentAssignmentResponseDTO responseDTO = studentAssignmentService.getStudentAssignmentById(studentAssignment.getId());

        // Assert
        assertNotNull(responseDTO);
        assertEquals(studentAssignment.getId(), responseDTO.getId());

        verify(studentAssignmentRepository).findById(studentAssignment.getId());
    }

    @Test
    public void testGetStudentAssignmentById_NotFound() {
        // Arrange
        when(studentAssignmentRepository.findById(studentAssignment.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                studentAssignmentService.getStudentAssignmentById(studentAssignment.getId()));

        assertEquals("Student Assignment not found with ID: " + studentAssignment.getId(), exception.getMessage());

        verify(studentAssignmentRepository).findById(studentAssignment.getId());
    }

    @Test
    public void testDeleteStudentAssignment_Success() {
        // Arrange
        when(studentAssignmentRepository.findById(studentAssignment.getId())).thenReturn(Optional.of(studentAssignment));
        doNothing().when(studentAssignmentRepository).delete(studentAssignment);

        // Act
        studentAssignmentService.deleteStudentAssignment(studentAssignment.getId());

        // Assert
        verify(studentAssignmentRepository).findById(studentAssignment.getId());
        verify(studentAssignmentRepository).delete(studentAssignment);
    }

    @Test
    public void testDeleteStudentAssignment_NotFound() {
        // Arrange
        when(studentAssignmentRepository.findById(studentAssignment.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                studentAssignmentService.deleteStudentAssignment(studentAssignment.getId()));

        assertEquals("Student Assignment not found with ID: " + studentAssignment.getId(), exception.getMessage());

        verify(studentAssignmentRepository).findById(studentAssignment.getId());
        verify(studentAssignmentRepository, never()).delete(any(StudentAssignment.class));
    }

    @Test
    public void testGetAssignmentsForStudent_Success() {
        // Arrange
        List<StudentAssignment> assignments = Arrays.asList(studentAssignment);
        when(studentAssignmentRepository.findByStudentId(student.getId())).thenReturn(assignments);

        // Act
        List<StudentAssignmentResponseDTO> responseDTOs = studentAssignmentService.getAssignmentsForStudent(student.getId());

        // Assert
        assertNotNull(responseDTOs);
        assertEquals(1, responseDTOs.size());
        assertEquals(studentAssignment.getId(), responseDTOs.get(0).getId());

        verify(studentAssignmentRepository).findByStudentId(student.getId());
    }

    @Test
    public void testGradeStudentAssignmentAndAddFeedback_NotFound() {
        // Arrange
        GradeAssignmentDTO gradeDTO = new GradeAssignmentDTO();
        gradeDTO.setGrade(95);
        gradeDTO.setFeedback("Good job!");

        when(studentAssignmentRepository.findById(studentAssignment.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                studentAssignmentService.gradeStudentAssignmentAndAddFeedback(
                        studentAssignment.getId(), gradeDTO, instructor.getId()));

        assertEquals("Student Assignment not found with ID: " + studentAssignment.getId(), exception.getMessage());

        verify(studentAssignmentRepository).findById(studentAssignment.getId());
        verify(studentAssignmentRepository, never()).save(any(StudentAssignment.class));
    }

    @Test
    public void testGradeStudentAssignmentAndAddFeedback_InvalidUser() {
        // Arrange
        GradeAssignmentDTO gradeDTO = new GradeAssignmentDTO();
        gradeDTO.setGrade(95);
        gradeDTO.setFeedback("Good job!");

        User anotherInstructor = new User();
        anotherInstructor.setId(99L); // Not the instructor of the course

        when(studentAssignmentRepository.findById(studentAssignment.getId())).thenReturn(Optional.of(studentAssignment));

        // Act & Assert
        InvalidUser exception = assertThrows(InvalidUser.class, () ->
                studentAssignmentService.gradeStudentAssignmentAndAddFeedback(
                        studentAssignment.getId(), gradeDTO, anotherInstructor.getId()));

        assertEquals("You are not the instructor of this course", exception.getMessage());

        verify(studentAssignmentRepository).findById(studentAssignment.getId());
        verify(studentAssignmentRepository, never()).save(any(StudentAssignment.class));
    }

}