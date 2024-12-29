package com.main.lms.controller;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.CreateStudentLessonDTO;
import com.main.lms.dtos.EnrollmentLessonResponseDTO;
import com.main.lms.entities.Course;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.StudentLesson;
import com.main.lms.entities.User;
import com.main.lms.repositories.CourseRepository;
import com.main.lms.services.StudentLessonService;
import com.main.lms.utility.SessionIdUtility;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student-lessons")
@RequiredArgsConstructor
public class StudentLessonController {

    private final StudentLessonService studentLessonService;
    private final SessionIdUtility sessionIdUtility;
    private final CourseRepository courseRepository;

    // student attend
    // sh8aalaa
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createStudentLesson(
            @RequestBody CreateStudentLessonDTO createStudentLessonDTO) {
        try {
            User user = sessionIdUtility.getUserFromSessionId().getUser();
            StudentLesson createdStudentLesson = studentLessonService.createStudentLesson(createStudentLessonDTO, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "Student Lesson created successfully",
                    new EnrollmentLessonResponseDTO(createdStudentLesson), null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Failed to create Student Lesson", null, new String[] { e.getMessage() }));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Failed to create Student Lesson", null, new String[] { e.getMessage() }));
        }
    }

    //sh8aalaa
    // inst
    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<EnrollmentLessonResponseDTO>> getLessonsByStudent(@PathVariable Long studentId) {
        List<StudentLesson> studentLessons = studentLessonService.getLessonsByStudent(studentId);
        List<EnrollmentLessonResponseDTO> responseDTOs = studentLessons.stream()
                .map(EnrollmentLessonResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    // inst
    //sh8aalaa
    @GetMapping("/students/{studentId}/courses/{courseId}")
    public ResponseEntity<ApiResponse<?>> getStudentLessonsInCourse(@PathVariable Long studentId,
            @PathVariable Long courseId) {
                try {
                    CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
                    List<StudentLesson> studentLessons = studentLessonService.getLessonsByStudentInCourse(studentId, courseId, user.getUser().getId());
                    List<EnrollmentLessonResponseDTO> responseDTOs = studentLessons.stream()
                            .map(EnrollmentLessonResponseDTO::new)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(new ApiResponse<>(true, "Student Lessons in Course", responseDTOs, null));
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body(
                            new ApiResponse<>(false, "Failed to get Student Lessons", null, new String[] { e.getMessage() }));
                }
    }

    //sh8aalaa
    // student
    @GetMapping("/students/me")
    public ResponseEntity<ApiResponse<List<EnrollmentLessonResponseDTO>>> meGetLessonsByStudent() {
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            List<StudentLesson> studentLessons = studentLessonService.getLessonsByStudent(user.getUser().getId());
            List<EnrollmentLessonResponseDTO> responseDTOs = studentLessons.stream()
                    .map(EnrollmentLessonResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Student Lessons", responseDTOs, null));
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Failed to get Student Lessons", null, new String[] { e.getMessage() }));
        }
    }

    // student
    //sh8aalaa
    @GetMapping("/students/me/courses/{courseId}")
    public ResponseEntity<ApiResponse<?>> meGetStudentLessonsInCourse(@PathVariable Long courseId) {
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
            List<StudentLesson> studentLessons = studentLessonService.getLessonsByStudentInCourse(user.getUser().getId(), courseId, course.getInstructor().getId());
            List<EnrollmentLessonResponseDTO> responseDTOs = studentLessons.stream()
                    .map(EnrollmentLessonResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Student Lessons in Course", responseDTOs, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Failed to get Student Lessons", null, new String[] { e.getMessage() }));
        }
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteStudentLesson(@PathVariable Long id) {
//        studentLessonService.deleteStudentLessonById(id);
//        return ResponseEntity.noContent().build();
//    }
}