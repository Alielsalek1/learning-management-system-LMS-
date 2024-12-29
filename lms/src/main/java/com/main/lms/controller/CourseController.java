package com.main.lms.controller;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.CourseRequestDTO;
import com.main.lms.dtos.CourseResponseDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.exceptions.CourseNotFoundException;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.services.CourseService;
import com.main.lms.utility.SessionIdUtility;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final SessionIdUtility sessionIdUtility;

    // instructor
    @PostMapping("/{id}/material")
    public ResponseEntity<ApiResponse<?>> createMaterial(@PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            courseService.addMaterials(id, sessionIdUtility.getUserFromSessionId().getUser().getId(), files);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (CourseNotFoundException exception) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Course not found", null, new String[] { "Course not found" }),
                    HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, exception.getMessage(), null, new String[] { exception.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // student, instructor
    @GetMapping("/{id}/material")
    public ResponseEntity<?> downloadMaterialsZip(@PathVariable Long id) {
        try {
            List<Path> paths = courseService.getCourseMaterials(id,
                    sessionIdUtility.getUserFromSessionId().getUser().getId());
            Resource zipResource = courseService.downloadMaterialsZip(paths, id);
            CourseResponseDTO course = courseService.getCourseById(id);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=materials" + "_" + course.getTitle().replace(" ", "-") + "_" + id + ".zip");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipResource.contentLength()));
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipResource);
        } catch (CourseNotFoundException exception) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Course not found", null, new String[] { "Course not found" }),
                    HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, exception.getMessage(), null, new String[] { exception.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // instructor
    // SH8AAAAAAAAAAAAAAALAAAAAAAAAAAA
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCourse(@RequestBody CourseRequestDTO course) {
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            CourseResponseDTO crs = courseService.addCourse(course.getTitle(), course.getDuration(),
                    course.getDescription(),
                    user.getUser().getId());
            return new ResponseEntity<>(new ApiResponse<>(true, "Course added successfully", crs, null),
                    HttpStatus.OK);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "User is not authenticated", null,
                            new String[] { "User is not authenticated" }),
                    HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    // any
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getAllCourses() {
//        return new ResponseEntity<>(
//                new ApiResponse<>(true, "Courses fetched successfully", courseService.getAllCourses(), null),
//                HttpStatus.OK);
//    }

    // any
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getCourseById(@PathVariable Long id) {
        try {
            CourseResponseDTO crs = courseService.getCourseById(id);
            return new ResponseEntity<>(new ApiResponse<>(true, "Course fetched successfully", crs, null),
                    HttpStatus.OK);
        } catch (CourseNotFoundException ex) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Course not found", null, new String[] { "Course not found" }),
                    HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, ex.getMessage(), null, new String[] { ex.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // instructor
    // SH8AAAAAAAAAAALAAAA
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCourse(@PathVariable Long id) {
        try {
            SessionIdUtility session = new SessionIdUtility();
            Long userId = session.getUserFromSessionId().getUser().getId();
            courseService.deleteCourseById(id, userId);
            return new ResponseEntity<>(new ApiResponse<>(true, "Course deleted successfully", null, null),
                    HttpStatus.NO_CONTENT);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[] { e.getMessage() }),
                    HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateCourse(@PathVariable Long id, @RequestBody CourseRequestDTO course) {
        try {
            CourseResponseDTO crs = courseService.updateCourse(id, course,
                    sessionIdUtility.getUserFromSessionId().getUser().getId());
            return new ResponseEntity<>(new ApiResponse<>(true, "Course updated successfully", crs, null),
                    HttpStatus.OK);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "You are not the instructor of this course", null, null),
                    HttpStatus.FORBIDDEN);
        } catch (CourseNotFoundException e) {
            return new ResponseEntity<>(
                    new ApiResponse<>(false, "Course not found", null, new String[] { "Course not found" }),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
