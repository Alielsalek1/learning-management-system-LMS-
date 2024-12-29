package com.main.lms.services;

import com.main.lms.dtos.StudentPerformanceDTO;
import com.main.lms.entities.*;
import com.main.lms.enums.QuestionType;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.*;
import com.main.lms.utility.ChartUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrolledCourseRepository enrollmentRepository;

    @Mock
    private StudentQuizRepository studentQuizRepository;

    @Mock
    private StudentAssignmentRepository studentAssignmentRepository;

    @Mock
    private StudentsLessonRepository studentsLessonRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private ChartUtility chartUtility;

    @InjectMocks
    private AnalyticsService analyticsService;

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
    public void testGetCourseAnalytics_AsAdmin_Success() {
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

        // Mock StudentQuizzes
        StudentQuiz studentQuiz1 = new StudentQuiz();
        studentQuiz1.setId(300L);
        studentQuiz1.setQuiz(quiz1);
        studentQuiz1.setStudent(studentUser);
        studentQuiz1.setGrade(1.0);
        List<StudentQuiz> studentQuizzes = Arrays.asList(studentQuiz1);

        when(studentQuizRepository.findByStudentAndQuiz_Course(studentUser, course)).thenReturn(studentQuizzes);

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

        when(studentAssignmentRepository.findByStudentAndCourse(studentUser, course)).thenReturn(studentAssignments);

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

        when(studentsLessonRepository.findByStudentAndLesson_Course(studentUser, course)).thenReturn(studentLessons);

        // Mock Questions for Quiz
        Question question1 = new Question();
        question1.setQuestionId(800L);
        question1.setQuestionContent("What is calculus?");
        question1.setType(QuestionType.MCQ);
        quiz1.setQuestions(Arrays.asList(question1));

        // Act
        List<StudentPerformanceDTO> result = analyticsService.getCourseAnalytics(courseId, user);

        // Assert
        Assertions.assertThat(result).hasSize(1);
        StudentPerformanceDTO dto = result.get(0);
        Assertions.assertThat(dto.getStudentId()).isEqualTo(3L);
        Assertions.assertThat(dto.getStudentName()).isEqualTo("Student User");

        double quizAverage = 100.0;
        Assertions.assertThat(dto.getQuizAverage()).isEqualTo(quizAverage);

        // Assignment average: (9 / 10) * 100 = 90.0%
        double assignmentAverage = 90.0;
        Assertions.assertThat(dto.getAssignmentAverage()).isEqualTo(assignmentAverage);

        // Attendance percentage: (1 attended / 1 total) * 100 = 100.0%
        double attendancePercentage = 100.0;
        Assertions.assertThat(dto.getAttendancePercentage()).isEqualTo(attendancePercentage);

        // Course completion status
        Assertions.assertThat(dto.getIsCourseCompleted()).isFalse();
    }

    @Test
    public void testGetCourseAnalytics_AsInstructor_Success() {
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

        // Mock StudentQuizzes
        StudentQuiz studentQuiz1 = new StudentQuiz();
        studentQuiz1.setId(300L);
        studentQuiz1.setQuiz(quiz1);
        studentQuiz1.setStudent(studentUser);
        studentQuiz1.setGrade(1.0);
        List<StudentQuiz> studentQuizzes = Arrays.asList(studentQuiz1);

        when(studentQuizRepository.findByStudentAndQuiz_Course(studentUser, course)).thenReturn(studentQuizzes);

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

        when(studentAssignmentRepository.findByStudentAndCourse(studentUser, course)).thenReturn(studentAssignments);

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

        when(studentsLessonRepository.findByStudentAndLesson_Course(studentUser, course)).thenReturn(studentLessons);

        // Mock Questions for Quiz
        Question question1 = new Question();
        question1.setQuestionId(800L);
        question1.setQuestionContent("Explain integral calculus.");
        question1.setType(QuestionType.SHORT_ANSWER);
        quiz1.setQuestions(Arrays.asList(question1));

        // Act
        List<StudentPerformanceDTO> result = analyticsService.getCourseAnalytics(courseId, user);

        // Assert
        Assertions.assertThat(result).hasSize(1);
        StudentPerformanceDTO dto = result.get(0);
        Assertions.assertThat(dto.getStudentId()).isEqualTo(3L);
        Assertions.assertThat(dto.getStudentName()).isEqualTo("Student User");

        double quizAverage = 100.0;
        Assertions.assertThat(dto.getQuizAverage()).isEqualTo(quizAverage);

        // Assignment average: (8 / 10) * 100 = 80.0%
        double assignmentAverage = 80.0;
        Assertions.assertThat(dto.getAssignmentAverage()).isEqualTo(assignmentAverage);

        // Attendance percentage: (1 attended / 1 total) * 100 = 100.0%
        double attendancePercentage = 100.0;
        Assertions.assertThat(dto.getAttendancePercentage()).isEqualTo(attendancePercentage);

        // Course completion status
        Assertions.assertThat(dto.getIsCourseCompleted()).isFalse();
    }

    @Test
    public void testGetCourseAnalytics_CourseNotFound_ThrowsException() {
        // Arrange
        Long courseId = 999L; // Non-existent course
        User user = adminUser;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> analyticsService.getCourseAnalytics(courseId, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with ID: " + courseId);
    }

    @Test
    public void testGetCourseAnalytics_UnauthorizedUser_ThrowsException() {
        // Arrange
        Long courseId = 100L;
        User unauthorizedUser = new User();
        unauthorizedUser.setId(4L);
        unauthorizedUser.setRole(UserRole.STUDENT);
        unauthorizedUser.setName("Unauthorized Student");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Act & Assert
        Assertions.assertThatThrownBy(() -> analyticsService.getCourseAnalytics(courseId, unauthorizedUser))
                .isInstanceOf(InvalidUser.class)
                .hasMessageContaining("You are not authorized to view this analytics data");
    }

    @Test
    public void testGetCourseAnalytics_NoQuizzes_AssignmentsOrLessons() {
        // Arrange
        Long courseId = 100L;
        User user = adminUser;

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourse(course)).thenReturn(enrolledCourses);

        // Mock empty quizzes, assignments, and lessons
        when(quizRepository.findByCourseId(courseId)).thenReturn(Collections.emptyList());
        when(studentQuizRepository.findByStudentAndQuiz_Course(studentUser, course)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(studentAssignmentRepository.findByStudentAndCourse(studentUser, course)).thenReturn(Collections.emptyList());
        when(lessonRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(studentsLessonRepository.findByStudentAndLesson_Course(studentUser, course)).thenReturn(Collections.emptyList());

        // Act
        List<StudentPerformanceDTO> result = analyticsService.getCourseAnalytics(courseId, user);

        // Assert
        Assertions.assertThat(result).hasSize(1);
        StudentPerformanceDTO dto = result.get(0);
        Assertions.assertThat(dto.getQuizAverage()).isEqualTo(0.0);
        Assertions.assertThat(dto.getAssignmentAverage()).isEqualTo(0.0);
        Assertions.assertThat(dto.getAttendancePercentage()).isEqualTo(0.0);
        Assertions.assertThat(dto.getIsCourseCompleted()).isFalse();
    }

}