package com.main.lms.controller;

import com.main.lms.dtos.*;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.services.StudentAssignmentService;
import com.main.lms.utility.SessionIdUtility;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/student-assignments")
@RequiredArgsConstructor
public class StudentAssignmentController {

        private final StudentAssignmentService studentAssignmentService;
        private final SessionIdUtility sessiondIdUtility;

        // student
        //sh8aaalaa
        @PostMapping
        public ResponseEntity<ApiResponse<?>> createStudentAssignment(@RequestBody CreateStudentAssignmentDTO dto) {
                try {

                        Long userId = sessiondIdUtility.getUserFromSessionId().getUser().getId();
                        StudentAssignmentResponseDTO newStudentAssignment = studentAssignmentService
                                        .createStudentAssignment(dto, userId);
                        return new ResponseEntity<>(
                                        new ApiResponse<>(true, "Student Assignment created successfully",
                                                        newStudentAssignment, null),
                                        HttpStatus.CREATED);
                } catch (Exception e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "Failed to create Student Assignment", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        //sh8aaalaaa
        // only student can submit assignment
        @PostMapping("/submissions/{id}")
        public ResponseEntity<ApiResponse<?>> uploadFile(@RequestParam("files") List<MultipartFile> files,
                        @PathVariable Long id) {
                try {

                        boolean result = studentAssignmentService.saveStudentSubmissionFile(files,
                                        id, sessiondIdUtility.getUserFromSessionId().getUser().getId());
                        if (!result) {
                                return new ResponseEntity<>(new ApiResponse<>(false, "Failed to upload file", null,
                                                new String[] { "File is empty" }), HttpStatus.BAD_REQUEST);
                        }
                        return new ResponseEntity<>(
                                        new ApiResponse<>(true, "File uploaded successfully", null, null),
                                        HttpStatus.NO_CONTENT);
                } catch (ClassCastException e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "User is not authenticated", null, null),
                                        HttpStatus.UNAUTHORIZED);
                } catch (NoSuchElementException e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "Submission record not found", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.NOT_FOUND);
                } catch (Exception e) {
                        e.printStackTrace();
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "Failed to upload file", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        //sh8aaalaa
        // student and instructor
        @GetMapping("/submissions/{id}")
        public ResponseEntity<?> downloadFiles(@PathVariable Long id) {
                try {

                        CustomUserDetails user = sessiondIdUtility.getUserFromSessionId();
                        List<Path> files = studentAssignmentService.getStudentSubmissions(id, user);
                        if (files.isEmpty()) {
                                return new ResponseEntity<>(new ApiResponse<>(false, "No files found", null, null),
                                                HttpStatus.NOT_FOUND);
                        }
                        Resource resource = studentAssignmentService.downloadStudentSubmissionsZip(files, id);
                        HttpHeaders headers = new HttpHeaders();
                        StudentAssignmentResponseDTO studentAssignment = studentAssignmentService
                                        .getStudentAssignmentById(id);
                        String fileName = "submissions_" + id + "_"
                                        + studentAssignment.getCourse().getTitle().replace(" ", "-") + ".zip";
                        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
                        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                        .body(resource);
                } catch (RuntimeException e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "You are not authorized to view this submission", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.UNAUTHORIZED);
                } catch (Exception e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "Failed to download file", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        // any
        // sh8aaalaaa
        @GetMapping("/courses/{courseId}")
        public ResponseEntity<ApiResponse<?>> getStudentAssignmentsByCourseId(@PathVariable Long courseId) {
                return new ResponseEntity<>(new ApiResponse<>(true, "Assignments for course found",
                                studentAssignmentService.getStudentAssignmentsByCourseId(courseId), new String[0]),
                                HttpStatus.OK);
        }

        // any
        //sh8aalaaa
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<?>> getStudentAssignmentById(@PathVariable Long id) {
                return new ResponseEntity<>(
                                new ApiResponse<>(true, "Student Assignment found",
                                                studentAssignmentService.getStudentAssignmentById(id), new String[0]),
                                HttpStatus.OK);
        }

        // sh8aalaaa
        // any
        @GetMapping("/users/me/courses/{courseId}")
        public ResponseEntity<ApiResponse<?>> getStudentAssignmentsByCourseIdAndUserId(@PathVariable Long courseId) {
                try {

                        Long userId = sessiondIdUtility.getUserFromSessionId().getUser().getId();
                        return new ResponseEntity<>(new ApiResponse<>(true, "Assignments for course found",
                                        studentAssignmentService.getStudentAssignmentByCourseIdAndUserId(courseId,
                                                        userId),
                                        new String[0]),
                                        HttpStatus.OK);
                } catch (NoSuchElementException e) {
                        return new ResponseEntity<>(new ApiResponse<>(false, "Assignment Submission not found", null,
                                        new String[] { e.getMessage() }), HttpStatus.NOT_FOUND);
                } catch (Exception e) {
                        return new ResponseEntity<>(new ApiResponse<>(false, "Failed to get assignments", null,
                                        new String[] { e.getMessage() }), HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        // // instructor
        // @DeleteMapping("/{id}")
        // public ResponseEntity<Void> deleteStudentAssignment(@PathVariable Long id) {
        // studentAssignmentService.deleteStudentAssignment(id);
        // return ResponseEntity.noContent().build();
        // }

        //sh8aalaaa
        // any
        @GetMapping("/users/{userId}")
        public ResponseEntity<ApiResponse<?>> getStudentAssignmentsByUserId(@PathVariable Long userId) {
                return new ResponseEntity<>(
                                new ApiResponse<>(true, "Assignments for user found",
                                                studentAssignmentService.getAssignmentsForStudent(userId),
                                                new String[0]),
                                HttpStatus.OK);
        }

        //sh8aala
        // instructor only
        @PutMapping("grade/{id}")
        public ResponseEntity<ApiResponse<?>> gradeStudentAssignment(@PathVariable Long id,
                        @RequestBody GradeAssignmentDTO dto) {
                ApiResponse<StudentAssignmentResponseDTO> response = new ApiResponse<>();
                try {
                        CustomUserDetails user = sessiondIdUtility.getUserFromSessionId();
                        response.setData(studentAssignmentService.gradeStudentAssignmentAndAddFeedback(id, dto,
                                        user.getUser().getId()));
                        response.setSuccess(true);
                        response.setMessage("Assignment graded successfully");
                        return ResponseEntity.ok(response);
                } catch (ClassCastException e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "User is not authenticated", null, null),
                                        HttpStatus.UNAUTHORIZED);
                } catch (InvalidUser e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "User is not authorized", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.FORBIDDEN);
                } catch (NoSuchElementException e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "Submission record not found", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.NOT_FOUND);
                } catch (Exception e) {
                        return new ResponseEntity<>(
                                        new ApiResponse<>(false, "Failed to upload file", null,
                                                        new String[] { e.getMessage() }),
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }
}
