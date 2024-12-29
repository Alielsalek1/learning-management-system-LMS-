package com.main.lms.controller;

import com.main.lms.dtos.*;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.QuizService;
import com.main.lms.utility.SessionIdUtility;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private final QuizService quizService;
    private final SessionIdUtility sessionIdUtility;

    @GetMapping("/me/quiz-grades")
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getQuizGradesForCurrentUser() {
        ApiResponse<List<GradeDTO>> response = new ApiResponse<>();
        try {
            
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            response.setSuccess(true);
            response.setMessage("Grades fetched successfully");
            response.setData(quizService.getStudentQuizGrades(user.getUser().getId()));
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            response.setSuccess(false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            response.setSuccess(false);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{studentId}/quiz-grades")
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getStudentQuizGrades(@PathVariable Long studentId) {
        ApiResponse<List<GradeDTO>> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            response.setSuccess(true);
            response.setMessage("Grades fetched successfully");
            response.setData(quizService.getStudentQuizGrades(studentId));
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            response.setSuccess(false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            response.setSuccess(false);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
