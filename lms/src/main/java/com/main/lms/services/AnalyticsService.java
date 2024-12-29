package com.main.lms.services;

import com.main.lms.dtos.StudentPerformanceDTO;
import com.main.lms.entities.*;
import com.main.lms.enums.UserRole;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.repositories.*;
import com.main.lms.utility.ChartUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CourseRepository courseRepository;
    private final EnrolledCourseRepository enrollmentRepository;
    private final StudentQuizRepository studentQuizRepository;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final StudentsLessonRepository studentsLessonRepository;
    private final QuizRepository quizRepository;
    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;
    private final ChartUtility chartUtility;


    public List<StudentPerformanceDTO> getCourseAnalytics(Long courseId, User user) {
        // Check authorization
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (!user.getRole().equals(UserRole.ADMIN) && !course.getInstructor().getId().equals(user.getId())) {
            throw new InvalidUser("You are not authorized to view this analytics data");
        }

        // Fetch all enrolled students and their completion status
        List<EnrolledCourse> enrolledCourses = enrollmentRepository.findByCourse(course);
        List<User> enrolledStudents = enrolledCourses.stream()
                .map(EnrolledCourse::getStudent)
                .toList();
        Map<Long, Boolean> studentCompletionMap = enrolledCourses.stream()
                .collect(Collectors.toMap(
                        ec -> ec.getStudent().getId(),
                        EnrolledCourse::getIsCompleted
                ));

        // Get all course activities
        List<Quiz> quizzes = quizRepository.findByCourseId(course.getId());
        List<Assignment> assignments = assignmentRepository.findByCourse(course);
        List<Lesson> lessons = lessonRepository.findByCourse(course);

        List<StudentPerformanceDTO> analyticsDataList = new ArrayList<>();

        for (User student : enrolledStudents) {
            StudentPerformanceDTO data = new StudentPerformanceDTO();
            data.setStudentId(student.getId());
            data.setStudentName(student.getName());

            // Calculate quiz average including unattempted quizzes as zero
            List<StudentQuiz> studentQuizzes = studentQuizRepository.findByStudentAndQuiz_Course(student, course);
            Map<Long, Double> studentQuizMap = studentQuizzes.stream()
                    .collect(Collectors.toMap(
                            sq -> sq.getQuiz().getQuizId(),
                            StudentQuiz::getGrade
                    ));


            double totalQuizPercentage = quizzes.stream()
                    .mapToDouble(quiz -> {
                        Double grade = studentQuizMap.getOrDefault(quiz.getQuizId(), 0.0);
                        double maxGrade = quiz.getQuestions().size();
                        return (maxGrade > 0) ? (grade / maxGrade) * 100.0 : 0.0;
                    })
                    .sum();
            double quizAverage = quizzes.isEmpty() ? 0.0 : totalQuizPercentage / quizzes.size();
            data.setQuizAverage(quizAverage);

            List<StudentAssignment> studentAssignments = studentAssignmentRepository.findByStudentAndCourse(student, course);
            Map<Long, Long> studentAssignmentMap = studentAssignments.stream()
                    .collect(Collectors.toMap(
                            sa -> sa.getAssignment().getAssignmentId(),
                            StudentAssignment::getGrade
                    ));

            double totalAssignmentPercentage = assignments.stream()
                    .mapToDouble(assignment -> {
                        Long gradeLong = studentAssignmentMap.getOrDefault(assignment.getAssignmentId(), 0L);
                        double grade = gradeLong.doubleValue();
                        int maxGrade = assignment.getMaxGrade();
                        return (maxGrade > 0) ? (grade / maxGrade) * 100.0 : 0.0;
                    })
                    .sum();
            double assignmentAverage = assignments.isEmpty() ? 0.0 : totalAssignmentPercentage / assignments.size();
            data.setAssignmentAverage(assignmentAverage); // Set as Double

            // Calculate attendance percentage
            List<StudentLesson> studentLessons = studentsLessonRepository.findByStudentAndLesson_Course(student, course);
            int totalLessons = lessons.size();
            int attendedLessons = studentLessons.size();
            Double attendancePercentage = (totalLessons > 0)
                    ? (attendedLessons * 100.0 / totalLessons) : 0.0;
            data.setAttendancePercentage(attendancePercentage);

            // Get course completion status
            Boolean isCompleted = studentCompletionMap.getOrDefault(student.getId(), false);
            data.setIsCourseCompleted(isCompleted);

            analyticsDataList.add(data);
        }

        return analyticsDataList;
    }

    public File generateCharts(Long courseId, User user) throws IOException {
        // Get analytics data
        List<StudentPerformanceDTO> analyticsDataList = getCourseAnalytics(courseId, user);

        // Prepare data for charts
        Map<String, Double> quizAverages = analyticsDataList.stream()
                .collect(Collectors.toMap(
                        StudentPerformanceDTO::getStudentName,
                        StudentPerformanceDTO::getQuizAverage
                ));

        Map<String, Double> assignmentAverages = analyticsDataList.stream()
                .collect(Collectors.toMap(
                        StudentPerformanceDTO::getStudentName,
                        dto -> dto.getAssignmentAverage().doubleValue()
                ));

        Map<String, Double> attendancePercentages = analyticsDataList.stream()
                .collect(Collectors.toMap(
                        StudentPerformanceDTO::getStudentName,
                        StudentPerformanceDTO::getAttendancePercentage
                ));

        // Define filenames for the charts
        String quizAveragesFile = "quiz_averages.png";
        String assignmentAveragesFile = "assignment_averages.png";
        String attendancePercentagesFile = "attendance_percentages.png";
        String courseCompletionFile = "course_completion.png";

        // Generate charts using ChartUtility
        chartUtility.generateBarChart(
                "Quiz Averages (%)",
                "Students",
                "Average Percentage",
                quizAverages,
                quizAveragesFile
        );

        chartUtility.generateBarChart(
                "Assignment Averages (%)",
                "Students",
                "Average Percentage",
                assignmentAverages,
                assignmentAveragesFile
        );

        chartUtility.generateBarChart(
                "Attendance Percentages (%)",
                "Students",
                "Attendance Percentage",
                attendancePercentages,
                attendancePercentagesFile
        );

        chartUtility.generateCompletionChart(
                "Course Completion Status",
                analyticsDataList,
                courseCompletionFile
        );
        // List of generated chart filenames
        List<String> chartFiles = List.of(
                quizAveragesFile,
                assignmentAveragesFile,
                attendancePercentagesFile,
                courseCompletionFile
        );

        String chartsDirPath = "charts/";
        String zipFileName = "charts_archive.zip";
        String zipFilePath = chartsDirPath + zipFileName;

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String fileName : chartFiles) {
                File fileToZip = new File(chartsDirPath + fileName);
                if (!fileToZip.exists()) {
                    continue; // Skip if the file does not exist
                }

                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }

        for (String fileName : chartFiles) {
            File fileToDelete = new File(chartsDirPath + fileName);
            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }
        }

        return new File(zipFilePath);
    }
}