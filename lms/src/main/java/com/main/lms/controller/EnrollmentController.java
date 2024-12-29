package com.main.lms.controller;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.EnrollmentRequestDTO;
import com.main.lms.dtos.EnrollmentResponseDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.EnrollmentService;
import com.main.lms.utility.SessionIdUtility;
import com.main.lms.utility.SessionIdUtility;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final SessionIdUtility sessionIdUtility;

    // student
    // SH8AAAAALAAAAA
    @PostMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<?>> createEnrollment(@PathVariable Long courseId) {
        try {
            EnrollmentResponseDTO response = enrollmentService.addEnrollment(courseId, sessionIdUtility.getUserFromSessionId().getUser().getId());
            return new ResponseEntity<>(new ApiResponse<>(true, "Student Enrolled Successfully", response, null),
                    HttpStatus.CREATED);

        } catch (ClassCastException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to enroll student", null, new String[]{"User is not logged in"}),
                    HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to enroll student", null, new String[]{e.getMessage()}),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // admin
    // SH8AAAAAAALAAAAAA
    @GetMapping
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getAllEnrollments() {
        return new ResponseEntity<>(
                new ApiResponse<>(true, "All Enrollments", enrollmentService.getAllEnrollments(), null),
                HttpStatus.OK);
    }

    // any
    // SH8AAAALAAAAAA
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getEnrollmentById(@PathVariable Long id) {
        try {
            EnrollmentResponseDTO response = enrollmentService.getEnrollmentById(id).orElse(null);
            if (response == null) {
                return new ResponseEntity<>(
                        new ApiResponse<>(false, "Enrollment not found", null, new String[]{"Enrollment not found"}),
                        HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ApiResponse<>(true, "Enrollment Details", response, null),
                    HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to get enrollment", null, new String[]{e.getMessage()}),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // any
    // sh8aaaalaaaaaa
    @GetMapping("/students/{studentId}/courses/{courseId}")
    public ResponseEntity<ApiResponse<?>> getEnrollmentByStudentAndCourse(
            @PathVariable Long studentId, @PathVariable Long courseId) {
        try {
            Optional<EnrollmentResponseDTO> response = enrollmentService.getEnrollmentByStudentAndCourse(studentId,
                    courseId);
            return new ResponseEntity<>(
                    new ApiResponse<>(true, "Enrollment for student and course", response, null),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to get enrollments", null, new String[]{e.getMessage()}),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // instructor & student to himself
    // SH8AAALAAAAA
    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentsByStudent(
            @PathVariable Long studentId) {
        try {
            List<EnrollmentResponseDTO> response = enrollmentService.getEnrollmentsByStudent(studentId);
            return new ResponseEntity<>(
                    new ApiResponse<>(true, "All enrollments for student", response, null),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to get enrollments", null, new String[]{e.getMessage()}),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // instructor
    // SH8AAAAAAALAAAAA
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getEnrollmentsByCourse(
            @PathVariable Long courseId) {
        try {
            List<EnrollmentResponseDTO> response = enrollmentService.getEnrollmentsByCourse(courseId);
            return new ResponseEntity<>(
                    new ApiResponse<>(true, "All enrollments for course", response, null),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to get enrollments", null, new String[]{e.getMessage()}),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // admin, instructor and student himself
    // SH8AAAAALAAAA
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteEnrollment(@PathVariable Long id) {
        try {
            SessionIdUtility session = new SessionIdUtility();
            CustomUserDetails user = session.getUserFromSessionId();
            enrollmentService.deleteEnrollmentById(id, user.getUser().getId());
            return new ResponseEntity<>(
                    new ApiResponse<>(true, "Student unenrolled successfully", null, null),
                    HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Failed to unenroll student", null, new String[]{e.getMessage()}),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to unenroll student", null, new String[]{e.getMessage()}),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // sh8aaalaaa
    // any
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateEnrollment(@PathVariable Long id, @RequestBody EnrollmentRequestDTO enrollmentRequestDTO) {
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            EnrollmentResponseDTO response = enrollmentService.updateEnrollmentById(id, user.getUser().getId(), enrollmentRequestDTO);
            return new ResponseEntity<>(
                    new ApiResponse<>(true, "Enrollment updated successfully", response, null),
                    HttpStatus.OK);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to update enrollment", null, new String[]{"User is not logged in"}),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to update enrollment", null, new String[]{e.getMessage()}),
                    HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to update enrollment", null, new String[]{e.getMessage()}),
                    HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to update enrollment", null, new String[]{e.getMessage()}),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Failed to update enrollment", null, new String[]{e.getMessage()}),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
