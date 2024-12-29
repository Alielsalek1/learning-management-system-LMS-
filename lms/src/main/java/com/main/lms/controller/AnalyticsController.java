package com.main.lms.controller;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.StudentPerformanceDTO;
import com.main.lms.entities.User;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.AnalyticsService;
import com.main.lms.services.ReportService;
import com.main.lms.utility.SessionIdUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SessionIdUtility sessionIdUtility;
    private final ReportService reportService;

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<List<StudentPerformanceDTO>>> getCourseAnalytics(@PathVariable Long courseId) {
        User user = sessionIdUtility.getUserFromSessionId().getUser();
        List<StudentPerformanceDTO> analyticsData = analyticsService.getCourseAnalytics(courseId, user);
        ApiResponse<List<StudentPerformanceDTO>> response = new ApiResponse<>();
        try {
            response.setSuccess(true);
            response.setData(analyticsData);
            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[]{e.getMessage()}),
                    HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException ex) {
            response.setMessage("Not Found");
            response.setErrors(new String[]{ex.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            response.setMessage("Internal Server Error");
            response.setErrors(new String[]{ex.getMessage()});
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/courses/{courseId}/charts")
    public ResponseEntity<?> generateCharts(@PathVariable Long courseId) {
        try {
            User user = sessionIdUtility.getUserFromSessionId().getUser();
            File chartsZip = analyticsService.generateCharts(courseId, user);

            if (chartsZip == null || !chartsZip.exists()) {
                ApiResponse<?> response = new ApiResponse<>();
                response.setSuccess(false);
                response.setMessage("Failed to generate charts.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            byte[] fileContent = Files.readAllBytes(chartsZip.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("charts_archive.zip")
                    .build());

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (ClassCastException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (ResourceNotFoundException e) {
            ApiResponse<?> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (InvalidUser e) {
            ApiResponse<?> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (IOException e) {
            ApiResponse<?> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Error processing the ZIP file.");
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            ApiResponse<?> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred.");
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/courses/{courseId}/performance-report")
    public ResponseEntity<?> generateStudentPerformanceReport(@PathVariable Long courseId) {
        try {
            User user = sessionIdUtility.getUserFromSessionId().getUser();

            ByteArrayInputStream reportStream = reportService.generateStudentPerformanceReport(courseId, user);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=student_performance_report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(reportStream));

        } catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[]{e.getMessage()}), HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[]{e.getMessage()}), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Error generating report", null, new String[]{e.getMessage()}), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}