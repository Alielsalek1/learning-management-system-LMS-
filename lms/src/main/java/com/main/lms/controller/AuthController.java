package com.main.lms.controller;

import java.util.Optional;

import com.main.lms.dtos.RegisterUserDTO;
import com.main.lms.dtos.UserResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main.lms.entities.User;
import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.LoginRequest;
import com.main.lms.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest loginRequest,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            Optional<User> user = userService.loginUser(loginRequest.getName(), loginRequest.getPassword());

            if (user.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken
                    .unauthenticated(loginRequest.getName(), loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(token);

            SecurityContext context = SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(context, request, response);
            return new ResponseEntity<>(
                    new ApiResponse<>(
                            true,
                            "Login successful",
                            UserResponseDTO.mapToResponseDTO(user.get()),
                            null),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() }),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterUserDTO requestBody) {
        try {
            User newUser = userService.registerUser(requestBody);
            return new ApiResponse<>(true, "Registration successful", UserResponseDTO.mapToResponseDTO(newUser), null);
        } catch (Exception e) {
            return new ApiResponse<>(false, e.getMessage(), null, new String[] { e.getMessage() });
        }
    }

}
