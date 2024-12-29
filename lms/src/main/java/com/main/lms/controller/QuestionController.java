package com.main.lms.controller;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.QuestionRequestDTO;
import com.main.lms.dtos.QuestionResponseDTO;
import com.main.lms.dtos.StudentPerformanceDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.enums.QuestionType;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.AnalyticsService;
import com.main.lms.services.QuestionService;
import com.main.lms.services.ReportService;
import com.main.lms.utility.SessionIdUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final SessionIdUtility sessionIdUtility;
    private final AnalyticsService analyticsService;
    private final ReportService reportService;

    //sh8aalaaa
    @PostMapping("/questions")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> createQuestion(@RequestBody @Valid QuestionRequestDTO requestDTO) {
        ApiResponse<QuestionResponseDTO> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            QuestionResponseDTO createdQuestion = questionService.mapToResponseDTO(questionService.createQuestion(requestDTO, user.getUser().getId()));
            response.setSuccess(true);
            response.setMessage("Question created successfully");
            response.setData(createdQuestion);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    // sh8aalaaa
    @GetMapping("/questions/course/{courseId}")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> getQuestionsByCourseId(@PathVariable Long courseId, @RequestParam Optional<QuestionType> questionType) {
        ApiResponse<List<QuestionResponseDTO>> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            List<QuestionResponseDTO> questions = questionService.getFilteredQuestions(courseId, questionType, user.getUser().getId())
                    .stream()
                    .map(questionService::mapToResponseDTO)
                    .toList();

            response.setSuccess(true);
            response.setMessage("Questions fetched successfully");
            response.setData(questions);
            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[]{e.getMessage()}),
                    HttpStatus.FORBIDDEN);
        } catch (CourseNotFoundException ex) {
            response.setMessage("Not Found");
            response.setErrors(new String[]{ex.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            response.setMessage("Internal Server Error");
            response.setErrors(new String[]{ex.getMessage()});
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // sh8aalaa
    @GetMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> getQuestionById(@PathVariable Long id) {
        ApiResponse<QuestionResponseDTO> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            QuestionResponseDTO fetchedQuestion = questionService.mapToResponseDTO(questionService.getQuestionById(id, user.getUser().getId()));
            response.setSuccess(true);
            response.setMessage("Question retrieved successfully");
            response.setData(fetchedQuestion);
            return ResponseEntity.ok(response);
        }  catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[] { e.getMessage() }),
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

    //sh8aalaaa
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        ApiResponse<Void> response = new ApiResponse<>();
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            questionService.deleteQuestion(id, user.getUser().getId());
            response.setSuccess(true);
            response.setMessage("Question deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "no", null, null),
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

}