package com.main.lms.controller;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.LessonRequestDTO;
import com.main.lms.dtos.LessonResponseDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.Lesson;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.LessonService;
import com.main.lms.utility.SessionIdUtility;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final SessionIdUtility sessionIdUtility;

    // Create a new lesson
    // instructor
    // SH8AAALAAAAA
    @PostMapping
    public ResponseEntity<ApiResponse<LessonResponseDTO>> createLesson(@RequestBody LessonRequestDTO lessonRequestDTO) {
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            Lesson createdLesson = lessonService.createLesson(lessonRequestDTO, user.getUser().getId());
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Lesson created successfully", new LessonResponseDTO(createdLesson), null));
        }  catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "no", null, null),
                    HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Failed to create lesson", null, new String[] { e.getMessage() }));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to create lesson", null, new String[] { e.getMessage() }));
        }
    }

//    // Retrieve all lessons
//    @GetMapping
//    public ResponseEntity<List<LessonResponseDTO>> getAllLessons() {
//        List<Lesson> lessons = lessonService.getAllLessons();
//        List<LessonResponseDTO> lessonDtos = lessons.stream().map(LessonResponseDTO::new).toList();
//        return ResponseEntity.ok(lessonDtos);
//    }

    // SH8AAALAAAA
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<LessonResponseDTO>> getLessonsByCourseId(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);
        List<LessonResponseDTO> lessonDtos = lessons.stream().map(LessonResponseDTO::new).toList();
        return ResponseEntity.ok(lessonDtos);
    }

    // all
    // Retrieve a lesson by ID
    // SH8AAALAAA
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponseDTO> getLessonById(@PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(new LessonResponseDTO(lesson));
    }

    // instructor
    // Update a lesson by ID
    //sh8aaalaaa
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateLesson(@PathVariable Long id,
            @RequestBody LessonRequestDTO lessonRequestDTO) {
        ApiResponse<Lesson> response = new ApiResponse<>();
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            Lesson updatedLesson = lessonService.updateLesson(id, lessonRequestDTO, user.getUser().getId());
            response.setSuccess(true);
            response.setMessage("Lesson updated successfully");
            response.setData(updatedLesson);
            return ResponseEntity.ok(response);
        }  catch (InvalidUser e) {
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

//    // Delete a lesson by ID
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
//        lessonService.deleteLesson(id);
//        return ResponseEntity.noContent().build();
//    }
}
