package com.main.lms.controller;

import com.main.lms.dtos.*;
import com.main.lms.entities.User;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.services.QuizService;
import com.main.lms.utility.SessionIdUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final SessionIdUtility sessionIdUtility;

    // sh8aalaa
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizDTO>> getQuizById(@PathVariable Long quizId) {
        ApiResponse<QuizDTO> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            QuizDTO quiz = quizService.mapToResponseDTO(quizService.getQuizById(quizId, user.getUser()));
            response.setSuccess(true);
            response.setMessage("Quiz fetched successfully");
            response.setData(quiz);
            return ResponseEntity.ok(response);
        }  catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[] { e.getMessage() }),
                    HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.badRequest().body(response);
        }
    }

    //sh8aalaaa
    @PostMapping()
    public ResponseEntity<ApiResponse<QuizDTO>> generateQuizForCourse(@RequestBody GenerateQuizRequestDTO request) {
        ApiResponse<QuizDTO> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            QuizDTO quiz = quizService.mapToResponseDTO(quizService.generateQuizForCourse(request, user.getUser()));
            response.setSuccess(true);
            response.setMessage("Quiz generated successfully");
            response.setData(quiz);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }  catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[] { e.getMessage() }),
                    HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<ApiResponse<GradeDTO>> submitQuiz(@PathVariable Long quizId, @RequestBody List<QuestionAnswerDTO> answers) {

        ApiResponse<GradeDTO> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            GradeDTO score = quizService.submitQuiz(quizId, user.getUser(), answers);
            response.setSuccess(true);
            response.setMessage("Quiz submitted successfully");
            response.setData(score);
            return ResponseEntity.ok(response);
        }  catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[] { e.getMessage() }),
                    HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.badRequest().body(response);
        }
    }

    // get quiz grades
    @GetMapping("/{quizId}/grades")
    public ResponseEntity<ApiResponse<List<GradeDTO>>> getQuizGrades(@PathVariable Long quizId) {

        ApiResponse<List<GradeDTO>> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            response.setSuccess(true);
            response.setMessage("Grades fetched successfully");
            response.setData(quizService.getQuizGrades(quizId, user.getUser()));
            return ResponseEntity.ok(response);
        }  catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[] { e.getMessage() }),
                    HttpStatus.FORBIDDEN);
        } catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            response.setSuccess(false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<List<Long>>> getQuizzesForCourse(@PathVariable Long courseId) {
        ApiResponse<List<Long>> response = new ApiResponse<>();
        response.setSuccess(false);
        try {
            User user = sessionIdUtility.getUserFromSessionId().getUser();

            List<Long> quizzes = quizService.getQuizzesForCourse(courseId, user).stream()
                    .map(quiz -> quiz.getQuizId())
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Quizzes fetched successfully");
            response.setData(quizzes);
            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (InvalidUser e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "User is not authorized", null, new String[]{e.getMessage()}),
                    HttpStatus.FORBIDDEN
            );
        } catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setMessage("An unexpected error occurred.");
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.badRequest().body(response);
        }
    }
}
