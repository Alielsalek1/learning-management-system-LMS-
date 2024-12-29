package com.main.lms.controller;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.AssignmentResponseDTO;
import com.main.lms.dtos.CreateAssignmentDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.exceptions.InvalidUser;
import com.main.lms.exceptions.ResourceNotFoundException;
import com.main.lms.services.AssignmentService;

import com.main.lms.utility.SessionIdUtility;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SessionIdUtility sessionIdUtility;

    // instructor
    //SH8AAAAALAAAAAAA
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAssignment(@RequestBody CreateAssignmentDTO dto) {
        ApiResponse<List<CreateAssignmentDTO>> response = new ApiResponse<>();
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            assignmentService.createAssignment(dto, user.getUser());
            response.setSuccess(true);
            response.setMessage("Assignment created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (ClassCastException e) {
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

    // all
    // sh8alAAAALAAAAAAA
    // 3. Retrieve Assignment by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getAssignmentById(@PathVariable Long id) {
        ApiResponse<AssignmentResponseDTO> response = new ApiResponse<>();
        try {
            AssignmentResponseDTO assignment = assignmentService.getAssignmentById(id);
            response.setSuccess(true);
            response.setMessage("Assignment found");
            response.setData(assignment);
            return ResponseEntity.ok(response);
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

    // instructor
    // SH8AALAAA
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateAssignment(@PathVariable Long id, @RequestBody CreateAssignmentDTO dto) {
        ApiResponse<AssignmentResponseDTO> response = new ApiResponse<>();
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            assignmentService.updateAssignment(id, dto, user.getUser());
            response.setSuccess(true);
            response.setMessage("Assignment Updated successfully");
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

    // instructor
    // sh8AALAAA
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAssignment(@PathVariable Long id) {
        ApiResponse<List<AssignmentResponseDTO>> response = new ApiResponse<>();
        try {
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            assignmentService.deleteAssignment(id, user.getUser());
            response.setSuccess(true);
            response.setMessage("Assignment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (InvalidUser e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authorized", null, new String[] { e.getMessage() }),
                    HttpStatus.FORBIDDEN);
        }  catch (ResourceNotFoundException e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            response.setErrors(new String[]{e.getMessage()});
            return ResponseEntity.badRequest().body(response);
        }
    }

    //SH8AAALAAA
    // any
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<?>> getAssignmentByCourseId(@PathVariable Long courseId) {
        ApiResponse<List<AssignmentResponseDTO>> response = new ApiResponse<>();
        try {
            List<AssignmentResponseDTO> assignments = assignmentService.getAssignmentbyCourseId(courseId);
            response.setSuccess(true);
            response.setMessage("Assignments retrieved successfully");
            response.setData(assignments);
            return ResponseEntity.ok(response);
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
}