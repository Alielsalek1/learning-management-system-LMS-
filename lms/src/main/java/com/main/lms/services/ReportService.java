package com.main.lms.services;

import com.main.lms.entities.*;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudentQuizRepository studentQuizRepository;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final StudentsLessonRepository studentLessonRepository;
    private final CourseRepository courseRepository;
    private final EnrolledCourseRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;

    public ByteArrayInputStream generateStudentPerformanceReport(Long courseId, User user) throws IOException {
        // Check authorization
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!user.getRole().equals(UserRole.ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new InvalidUser("You are not authorized to generate this report");
        }

        // Fetch all enrolled students
        List<EnrolledCourse> enrolledCourses = enrollmentRepository.findByCourse(course);
        List<User> enrolledStudents = enrolledCourses.stream()
                .map(EnrolledCourse::getStudent)
                .toList();

        // Create Excel workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Create sheets and populate data
        createQuizSheet(workbook, enrolledStudents, course);
        createAssignmentSheet(workbook, enrolledStudents, course);
        createAttendanceSheet(workbook, enrolledStudents, course);

        // Write to ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void createQuizSheet(XSSFWorkbook workbook, List<User> enrolledStudents, Course course) {
        Sheet sheet = workbook.createSheet("Quizzes");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Student ID");
        header.createCell(1).setCellValue("Student Name");
        header.createCell(2).setCellValue("Quiz ID");
        header.createCell(3).setCellValue("Grade");
        header.createCell(4).setCellValue("Max Grade");

        int rowNum = 1;
        // Get all quizzes of the course
        List<Quiz> quizzes = quizRepository.findByCourseId(course.getId());

        for (User student : enrolledStudents) {
            for (Quiz quiz : quizzes) {
                // Fetch student quiz record
                Optional<StudentQuiz> studentQuizOpt = studentQuizRepository.findByStudentAndQuiz(student, quiz);
                Double grade = studentQuizOpt.map(StudentQuiz::getGrade).orElse(0.0);
                var maxGrade = quiz.getQuestions().size();

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(quiz.getQuizId());
                row.createCell(3).setCellValue(grade);
                row.createCell(4).setCellValue(maxGrade);
            }
        }
        for (int i = 0; i <= 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAssignmentSheet(XSSFWorkbook workbook, List<User> enrolledStudents, Course course) {
        Sheet sheet = workbook.createSheet("Assignments");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Student ID");
        header.createCell(1).setCellValue("Student Name");
        header.createCell(2).setCellValue("Assignment ID");
        header.createCell(3).setCellValue("Grade");
        header.createCell(4).setCellValue("Max Grade");

        int rowNum = 1;
        // Get all assignments of the course
        List<Assignment> assignments = assignmentRepository.findByCourse(course);

        for (User student : enrolledStudents) {
            for (Assignment assignment : assignments) {
                // Fetch student assignment record
                Optional<StudentAssignment> studentAssignmentOpt = studentAssignmentRepository.findByAssignmentAndStudent(assignment, student);
                Long grade = studentAssignmentOpt.map(StudentAssignment::getGrade).orElse(0L);
                var maxGrade = assignment.getMaxGrade();

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(assignment.getAssignmentId());
                row.createCell(3).setCellValue(grade);
                row.createCell(4).setCellValue(maxGrade);
            }
        }
        for (int i = 0; i <= 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAttendanceSheet(XSSFWorkbook workbook, List<User> enrolledStudents, Course course) {
        Sheet sheet = workbook.createSheet("Attendance");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Student ID");
        header.createCell(1).setCellValue("Student Name");
        header.createCell(2).setCellValue("Lesson ID");
        header.createCell(3).setCellValue("Status");

        int rowNum = 1;
        // Get all lessons of the course
        List<Lesson> lessons = lessonRepository.findByCourse(course);

        for (User student : enrolledStudents) {
            for (Lesson lesson : lessons) {
                // Check if the student attended the lesson
                Optional<StudentLesson> attendanceOpt = studentLessonRepository.findByStudentAndLesson(student, lesson);
                String status = attendanceOpt.isPresent() ? "Present" : "Absent";

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(lesson.getLessonId());
                row.createCell(3).setCellValue(status);
            }
        }
        for (int i = 0; i <= 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}