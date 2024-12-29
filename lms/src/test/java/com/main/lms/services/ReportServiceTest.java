package com.main.lms.services;

import com.main.lms.entities.*;
import com.main.lms.enums.QuestionType;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private StudentQuizRepository studentQuizRepository;

    @Mock
    private StudentAssignmentRepository studentAssignmentRepository;

    @Mock
    private StudentsLessonRepository studentLessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrolledCourseRepository enrollmentRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private ReportService reportService;

    private Course course;
    private User adminUser;
    private User instructorUser;
    private User studentUser;
    private List<EnrolledCourse> enrolledCourses;

    @BeforeEach
    public void setUp() {
        // Initialize Users
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setName("Admin User");

        instructorUser = new User();
        instructorUser.setId(2L);
        instructorUser.setRole(UserRole.INSTRUCTOR);
        instructorUser.setName("Instructor User");

        studentUser = new User();
        studentUser.setId(3L);
        studentUser.setRole(UserRole.STUDENT);
        studentUser.setName("Student User");

        // Initialize Course
        course = new Course();
        course.setId(100L);
        course.setTitle("Advanced Mathematics");
        course.setInstructor(instructorUser);

        // Initialize EnrolledCourses
        EnrolledCourse enrolledCourse1 = new EnrolledCourse();
        enrolledCourse1.setId(1001L);
        enrolledCourse1.setCourse(course);
        enrolledCourse1.setStudent(studentUser);
        enrolledCourse1.setIsCompleted(false);

        enrolledCourses = Arrays.asList(enrolledCourse1);
    }

    @Test
    public void testGenerateStudentPerformanceReport_AsAdmin_Success() throws IOException {
        // Arrange
        Long courseId = 100L;
        User user = adminUser;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourse(course)).thenReturn(enrolledCourses);

        // Mock Quizzes
        Quiz quiz1 = new Quiz();
        quiz1.setQuizId(200L);
        quiz1.setCourse(course);
        List<Quiz> quizzes = Arrays.asList(quiz1);

        when(quizRepository.findByCourseId(courseId)).thenReturn(quizzes);

        // Mock Questions for Quiz
        Question question1 = new Question();
        question1.setQuestionId(800L);
        question1.setQuestionContent("What is calculus?");
        question1.setType(QuestionType.MCQ);
        quiz1.setQuestions(Arrays.asList(question1));

        // Mock StudentQuizzes
        StudentQuiz studentQuiz1 = new StudentQuiz();
        studentQuiz1.setId(300L);
        studentQuiz1.setQuiz(quiz1);
        studentQuiz1.setStudent(studentUser);
        studentQuiz1.setGrade(8.0);
        List<StudentQuiz> studentQuizzes = Arrays.asList(studentQuiz1);

        when(studentQuizRepository.findByStudentAndQuiz(studentUser, quiz1)).thenReturn(Optional.of(studentQuiz1));

        // Mock Assignments
        Assignment assignment1 = new Assignment();
        assignment1.setAssignmentId(400L);
        assignment1.setCourse(course);
        assignment1.setMaxGrade(10);
        List<Assignment> assignments = Arrays.asList(assignment1);

        when(assignmentRepository.findByCourse(course)).thenReturn(assignments);

        // Mock StudentAssignments
        StudentAssignment studentAssignment1 = new StudentAssignment();
        studentAssignment1.setId(500L);
        studentAssignment1.setAssignment(assignment1);
        studentAssignment1.setStudent(studentUser);
        studentAssignment1.setGrade(9L);
        List<StudentAssignment> studentAssignments = Arrays.asList(studentAssignment1);

        when(studentAssignmentRepository.findByAssignmentAndStudent(assignment1, studentUser)).thenReturn(Optional.of(studentAssignment1));

        // Mock Lessons
        Lesson lesson1 = new Lesson();
        lesson1.setLessonId(600L);
        lesson1.setCourse(course);
        lesson1.setOtp("OTP123");
        List<Lesson> lessons = Arrays.asList(lesson1);

        when(lessonRepository.findByCourse(course)).thenReturn(lessons);

        // Mock StudentLessons
        StudentLesson studentLesson1 = new StudentLesson();
        studentLesson1.setStudentLessonId(700L);
        studentLesson1.setStudent(studentUser);
        studentLesson1.setLesson(lesson1);
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson1);

        when(studentLessonRepository.findByStudentAndLesson(studentUser, lesson1)).thenReturn(Optional.of(studentLesson1));

        // Act
        ByteArrayInputStream reportStream = reportService.generateStudentPerformanceReport(courseId, user);

        // Assert
        Assertions.assertThat(reportStream).isNotNull();

        // Read the workbook from the generated stream
        Workbook workbook = new XSSFWorkbook(reportStream);

        // Validate Quizzes Sheet
        Sheet quizSheet = workbook.getSheet("Quizzes");
        Assertions.assertThat(quizSheet).isNotNull();
        Row quizHeader = quizSheet.getRow(0);
        Assertions.assertThat(quizHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(quizHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(quizHeader.getCell(2).getStringCellValue()).isEqualTo("Quiz ID");
        Assertions.assertThat(quizHeader.getCell(3).getStringCellValue()).isEqualTo("Grade");
        Assertions.assertThat(quizHeader.getCell(4).getStringCellValue()).isEqualTo("Max Grade");

        Row quizData = quizSheet.getRow(1);
        Assertions.assertThat(quizData.getCell(0).getNumericCellValue()).isEqualTo((double) studentUser.getId());
        Assertions.assertThat(quizData.getCell(1).getStringCellValue()).isEqualTo(studentUser.getName());
        Assertions.assertThat(quizData.getCell(2).getNumericCellValue()).isEqualTo((double) quiz1.getQuizId());
        Assertions.assertThat(quizData.getCell(3).getNumericCellValue()).isEqualTo((double) studentQuiz1.getGrade());
        Assertions.assertThat(quizData.getCell(4).getNumericCellValue()).isEqualTo(quiz1.getQuestions().size());

        // Validate Assignments Sheet
        Sheet assignmentSheet = workbook.getSheet("Assignments");
        Assertions.assertThat(assignmentSheet).isNotNull();
        Row assignmentHeader = assignmentSheet.getRow(0);
        Assertions.assertThat(assignmentHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(assignmentHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(assignmentHeader.getCell(2).getStringCellValue()).isEqualTo("Assignment ID");
        Assertions.assertThat(assignmentHeader.getCell(3).getStringCellValue()).isEqualTo("Grade");
        Assertions.assertThat(assignmentHeader.getCell(4).getStringCellValue()).isEqualTo("Max Grade");

        Row assignmentData = assignmentSheet.getRow(1);
        Assertions.assertThat(assignmentData.getCell(0).getNumericCellValue()).isEqualTo((double) studentUser.getId());
        Assertions.assertThat(assignmentData.getCell(1).getStringCellValue()).isEqualTo(studentUser.getName());
        Assertions.assertThat(assignmentData.getCell(2).getNumericCellValue()).isEqualTo((double) assignment1.getAssignmentId());
        Assertions.assertThat(assignmentData.getCell(3).getNumericCellValue()).isEqualTo((double) studentAssignment1.getGrade());
        Assertions.assertThat(assignmentData.getCell(4).getNumericCellValue()).isEqualTo(assignment1.getMaxGrade());

        // Validate Attendance Sheet
        Sheet attendanceSheet = workbook.getSheet("Attendance");
        Assertions.assertThat(attendanceSheet).isNotNull();
        Row attendanceHeader = attendanceSheet.getRow(0);
        Assertions.assertThat(attendanceHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(attendanceHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(attendanceHeader.getCell(2).getStringCellValue()).isEqualTo("Lesson ID");
        Assertions.assertThat(attendanceHeader.getCell(3).getStringCellValue()).isEqualTo("Status");

        Row attendanceData = attendanceSheet.getRow(1);
        Assertions.assertThat(attendanceData.getCell(0).getNumericCellValue()).isEqualTo((double) studentUser.getId());
        Assertions.assertThat(attendanceData.getCell(1).getStringCellValue()).isEqualTo(studentUser.getName());
        Assertions.assertThat(attendanceData.getCell(2).getNumericCellValue()).isEqualTo((double) lesson1.getLessonId());
        Assertions.assertThat(attendanceData.getCell(3).getStringCellValue()).isEqualTo("Present");

        workbook.close();
    }

    @Test
    public void testGenerateStudentPerformanceReport_AsInstructor_Success() throws IOException {
        // Arrange
        Long courseId = 100L;
        User user = instructorUser;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourse(course)).thenReturn(enrolledCourses);

        // Mock Quizzes
        Quiz quiz1 = new Quiz();
        quiz1.setQuizId(200L);
        quiz1.setCourse(course);
        List<Quiz> quizzes = Arrays.asList(quiz1);

        when(quizRepository.findByCourseId(courseId)).thenReturn(quizzes);

        // Mock Questions for Quiz
        Question question1 = new Question();
        question1.setQuestionId(800L);
        question1.setQuestionContent("Explain integral calculus.");
        question1.setType(QuestionType.MCQ);
        quiz1.setQuestions(Arrays.asList(question1));

        // Mock StudentQuizzes
        StudentQuiz studentQuiz1 = new StudentQuiz();
        studentQuiz1.setId(300L);
        studentQuiz1.setQuiz(quiz1);
        studentQuiz1.setStudent(studentUser);
        studentQuiz1.setGrade(7.0);
        List<StudentQuiz> studentQuizzes = Arrays.asList(studentQuiz1);

        when(studentQuizRepository.findByStudentAndQuiz(studentUser, quiz1)).thenReturn(Optional.of(studentQuiz1));

        // Mock Assignments
        Assignment assignment1 = new Assignment();
        assignment1.setAssignmentId(400L);
        assignment1.setCourse(course);
        assignment1.setMaxGrade(10);
        List<Assignment> assignments = Arrays.asList(assignment1);

        when(assignmentRepository.findByCourse(course)).thenReturn(assignments);

        // Mock StudentAssignments
        StudentAssignment studentAssignment1 = new StudentAssignment();
        studentAssignment1.setId(500L);
        studentAssignment1.setAssignment(assignment1);
        studentAssignment1.setStudent(studentUser);
        studentAssignment1.setGrade(8L);
        List<StudentAssignment> studentAssignments = Arrays.asList(studentAssignment1);

        when(studentAssignmentRepository.findByAssignmentAndStudent(assignment1, studentUser)).thenReturn(Optional.of(studentAssignment1));

        // Mock Lessons
        Lesson lesson1 = new Lesson();
        lesson1.setLessonId(600L);
        lesson1.setCourse(course);
        lesson1.setOtp("OTP123");
        List<Lesson> lessons = Arrays.asList(lesson1);

        when(lessonRepository.findByCourse(course)).thenReturn(lessons);

        // Mock StudentLessons
        StudentLesson studentLesson1 = new StudentLesson();
        studentLesson1.setStudentLessonId(700L);
        studentLesson1.setStudent(studentUser);
        studentLesson1.setLesson(lesson1);
        List<StudentLesson> studentLessons = Arrays.asList(studentLesson1);

        when(studentLessonRepository.findByStudentAndLesson(studentUser, lesson1)).thenReturn(Optional.of(studentLesson1));

        // Act
        ByteArrayInputStream reportStream = reportService.generateStudentPerformanceReport(courseId, user);

        // Assert
        Assertions.assertThat(reportStream).isNotNull();

        // Read the workbook from the generated stream
        Workbook workbook = new XSSFWorkbook(reportStream);

        // Validate Quizzes Sheet
        Sheet quizSheet = workbook.getSheet("Quizzes");
        Assertions.assertThat(quizSheet).isNotNull();
        Row quizHeader = quizSheet.getRow(0);
        Assertions.assertThat(quizHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(quizHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(quizHeader.getCell(2).getStringCellValue()).isEqualTo("Quiz ID");
        Assertions.assertThat(quizHeader.getCell(3).getStringCellValue()).isEqualTo("Grade");
        Assertions.assertThat(quizHeader.getCell(4).getStringCellValue()).isEqualTo("Max Grade");

        Row quizData = quizSheet.getRow(1);
        Assertions.assertThat(quizData.getCell(0).getNumericCellValue()).isEqualTo((double) studentUser.getId());
        Assertions.assertThat(quizData.getCell(1).getStringCellValue()).isEqualTo(studentUser.getName());
        Assertions.assertThat(quizData.getCell(2).getNumericCellValue()).isEqualTo((double) quiz1.getQuizId());
        Assertions.assertThat(quizData.getCell(3).getNumericCellValue()).isEqualTo((double) studentQuiz1.getGrade());
        Assertions.assertThat(quizData.getCell(4).getNumericCellValue()).isEqualTo(quiz1.getQuestions().size());

        // Validate Assignments Sheet
        Sheet assignmentSheet = workbook.getSheet("Assignments");
        Assertions.assertThat(assignmentSheet).isNotNull();
        Row assignmentHeader = assignmentSheet.getRow(0);
        Assertions.assertThat(assignmentHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(assignmentHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(assignmentHeader.getCell(2).getStringCellValue()).isEqualTo("Assignment ID");
        Assertions.assertThat(assignmentHeader.getCell(3).getStringCellValue()).isEqualTo("Grade");
        Assertions.assertThat(assignmentHeader.getCell(4).getStringCellValue()).isEqualTo("Max Grade");

        Row assignmentData = assignmentSheet.getRow(1);
        Assertions.assertThat(assignmentData.getCell(0).getNumericCellValue()).isEqualTo((double) studentUser.getId());
        Assertions.assertThat(assignmentData.getCell(1).getStringCellValue()).isEqualTo(studentUser.getName());
        Assertions.assertThat(assignmentData.getCell(2).getNumericCellValue()).isEqualTo((double) assignment1.getAssignmentId());
        Assertions.assertThat(assignmentData.getCell(3).getNumericCellValue()).isEqualTo((double) studentAssignment1.getGrade());
        Assertions.assertThat(assignmentData.getCell(4).getNumericCellValue()).isEqualTo(assignment1.getMaxGrade());

        // Validate Attendance Sheet
        Sheet attendanceSheet = workbook.getSheet("Attendance");
        Assertions.assertThat(attendanceSheet).isNotNull();
        Row attendanceHeader = attendanceSheet.getRow(0);
        Assertions.assertThat(attendanceHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(attendanceHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(attendanceHeader.getCell(2).getStringCellValue()).isEqualTo("Lesson ID");
        Assertions.assertThat(attendanceHeader.getCell(3).getStringCellValue()).isEqualTo("Status");

        Row attendanceData = attendanceSheet.getRow(1);
        Assertions.assertThat(attendanceData.getCell(0).getNumericCellValue()).isEqualTo((double) studentUser.getId());
        Assertions.assertThat(attendanceData.getCell(1).getStringCellValue()).isEqualTo(studentUser.getName());
        Assertions.assertThat(attendanceData.getCell(2).getNumericCellValue()).isEqualTo((double) lesson1.getLessonId());
        Assertions.assertThat(attendanceData.getCell(3).getStringCellValue()).isEqualTo("Present");

        workbook.close();
    }

    @Test
    public void testGenerateStudentPerformanceReport_CourseNotFound_ThrowsException() {
        // Arrange
        Long courseId = 999L; // Non-existent course
        User user = adminUser;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> reportService.generateStudentPerformanceReport(courseId, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with ID: " + courseId);
    }

    @Test
    public void testGenerateStudentPerformanceReport_UnauthorizedUser_ThrowsException() {
        // Arrange
        Long courseId = 100L;
        User unauthorizedUser = new User();
        unauthorizedUser.setId(4L);
        unauthorizedUser.setRole(UserRole.STUDENT);
        unauthorizedUser.setName("Unauthorized Student");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Act & Assert
        Assertions.assertThatThrownBy(() -> reportService.generateStudentPerformanceReport(courseId, unauthorizedUser))
                .isInstanceOf(InvalidUser.class)
                .hasMessageContaining("You are not authorized to generate this report");
    }

    @Test
    public void testGenerateStudentPerformanceReport_NoQuizzes_AssignmentsOrLessons() throws IOException {
        // Arrange
        Long courseId = 100L;
        User user = adminUser;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourse(course)).thenReturn(enrolledCourses);

        // Mock empty quizzes, assignments, and lessons
        when(quizRepository.findByCourseId(courseId)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(lessonRepository.findByCourse(course)).thenReturn(Collections.emptyList());

        // Act
        ByteArrayInputStream reportStream = reportService.generateStudentPerformanceReport(courseId, user);

        // Assert
        Assertions.assertThat(reportStream).isNotNull();

        // Read the workbook from the generated stream
        Workbook workbook = new XSSFWorkbook(reportStream);

        // Validate Quizzes Sheet
        Sheet quizSheet = workbook.getSheet("Quizzes");
        Assertions.assertThat(quizSheet).isNotNull();
        Row quizHeader = quizSheet.getRow(0);
        Assertions.assertThat(quizHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(quizHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(quizHeader.getCell(2).getStringCellValue()).isEqualTo("Quiz ID");
        Assertions.assertThat(quizHeader.getCell(3).getStringCellValue()).isEqualTo("Grade");
        Assertions.assertThat(quizHeader.getCell(4).getStringCellValue()).isEqualTo("Max Grade");

        // Since there are no quizzes, there should be no data rows
        Assertions.assertThat(quizSheet.getLastRowNum()).isEqualTo(0);

        // Validate Assignments Sheet
        Sheet assignmentSheet = workbook.getSheet("Assignments");
        Assertions.assertThat(assignmentSheet).isNotNull();
        Row assignmentHeader = assignmentSheet.getRow(0);
        Assertions.assertThat(assignmentHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(assignmentHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(assignmentHeader.getCell(2).getStringCellValue()).isEqualTo("Assignment ID");
        Assertions.assertThat(assignmentHeader.getCell(3).getStringCellValue()).isEqualTo("Grade");
        Assertions.assertThat(assignmentHeader.getCell(4).getStringCellValue()).isEqualTo("Max Grade");

        Assertions.assertThat(assignmentSheet.getLastRowNum()).isEqualTo(0);

        Sheet attendanceSheet = workbook.getSheet("Attendance");
        Assertions.assertThat(attendanceSheet).isNotNull();
        Row attendanceHeader = attendanceSheet.getRow(0);
        Assertions.assertThat(attendanceHeader.getCell(0).getStringCellValue()).isEqualTo("Student ID");
        Assertions.assertThat(attendanceHeader.getCell(1).getStringCellValue()).isEqualTo("Student Name");
        Assertions.assertThat(attendanceHeader.getCell(2).getStringCellValue()).isEqualTo("Lesson ID");
        Assertions.assertThat(attendanceHeader.getCell(3).getStringCellValue()).isEqualTo("Status");

        Assertions.assertThat(attendanceSheet.getLastRowNum()).isEqualTo(0);

        workbook.close();
    }
}