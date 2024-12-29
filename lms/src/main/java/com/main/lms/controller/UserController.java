package com.main.lms.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.RegisterUserDTO;
import com.main.lms.dtos.UpdateUserDTO;
import com.main.lms.dtos.UserResponseDTO;
import com.main.lms.services.UserService;
import com.main.lms.utility.SessionIdUtility;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<?>> getAuthenticatedUser() {
        try {
            SessionIdUtility sessionIdUtility = new SessionIdUtility();
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            UserResponseDTO userResponseDTO = UserResponseDTO.mapToResponseDTO(userService.findUserById(user.getUser().getId()).get());
            return new ResponseEntity<>(new ApiResponse<>(true, "User found", userResponseDTO, null), HttpStatus.OK);

        } catch (ClassCastException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/user/me")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UpdateUserDTO requestBody) {
        try {
            SessionIdUtility sessionIdUtility = new SessionIdUtility();
            CustomUserDetails user = sessionIdUtility.getUserFromSessionId();
            if (user == null) {
                return new ResponseEntity<ApiResponse<?>>(
                        new ApiResponse<>(false, "User not found", null, new String[] { "User not found" }),
                        HttpStatus.NOT_FOUND);
            }
            Optional<User> newUser = userService.updateUser(user.getUser().getId(), requestBody);
            if(newUser.isEmpty()){
                return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, "User not found", null, new String[] { "User not found" }),
                        HttpStatus.NOT_FOUND);
            }
            UserResponseDTO userResponseDTO = UserResponseDTO.mapToResponseDTO(newUser.get());
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(true, "User updated successfully", userResponseDTO, null),
                    HttpStatus.OK);
        } catch (ClassCastException e) {
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, "User is not authenticated", null, null),
                    HttpStatus.UNAUTHORIZED);
        }
        catch (Exception e) {
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userService.findUserById(id);
            if (user.isEmpty()) {
                return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, "User not found", null,
                        new String[] { "User not found" }), HttpStatus.NOT_FOUND);
            }
            UserResponseDTO userResponseDTO = UserResponseDTO.mapToResponseDTO(user.get());
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(true, "User found", userResponseDTO, null),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            return new ResponseEntity<ApiResponse<?>>(
                    new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<?>> updateUserById(@PathVariable Long id,
            @RequestBody UpdateUserDTO requestBody) {
        try {
            Optional<User> user = userService.updateUser(id, requestBody);
            if (user.isEmpty()) {
                return new ResponseEntity<ApiResponse<?>>(
                        new ApiResponse<>(false, "User not found", null, new String[] { "User not found" }),
                        HttpStatus.NOT_FOUND);
            }
            UserResponseDTO userResponseDTO = UserResponseDTO.mapToResponseDTO(user.get());

            return new ResponseEntity<ApiResponse<?>>(
                    new ApiResponse<>(true, "User updated successfully", userResponseDTO, null),
                    HttpStatus.OK);
        } catch(RuntimeException e)
        {
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<ApiResponse<?>>(
                    new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody RegisterUserDTO requestBody) {
        try {
            User user = userService.createUser(requestBody);
            UserResponseDTO userResponseDTO = UserResponseDTO.mapToResponseDTO(user);
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(true, "User created successfully", userResponseDTO, null),
                    HttpStatus.CREATED);
        } catch(RuntimeException e){
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            return new ResponseEntity<ApiResponse<?>>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
