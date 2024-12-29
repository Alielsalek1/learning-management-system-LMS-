package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.*;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.*;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController using @MockBean.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @SuppressWarnings("removal")
        @MockBean
        private UserService userService;

        @SuppressWarnings("removal")
        @MockBean
        private AuthenticationManager authenticationManager;
        
        @SuppressWarnings("removal")
        @MockBean
        private SecurityContextRepository securityContextRepository;

        private ObjectMapper objectMapper = new ObjectMapper();

        private User testUser;

        @BeforeEach
        public void setUp() {
                // Initialize test user
                testUser = new User();
                testUser.setId(1L);
                testUser.setName("testuser");
                testUser.setPassword("password");
                testUser.setRole(UserRole.STUDENT);
        }

        @Test
        public void testLogin_Success() throws Exception {
                // Arrange
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setName("testuser");
                loginRequest.setPassword("password");

                when(userService.loginUser("testuser", "password")).thenReturn(Optional.of(testUser));

                Authentication authentication = mock(Authentication.class);
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(authentication);

                doNothing().when(securityContextRepository).saveContext(any(SecurityContext.class),
                                any(HttpServletRequest.class), any(HttpServletResponse.class));

                // Act & Assert
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Login successful"))
                                .andExpect(jsonPath("$.data.name").value("testuser"));
        }

        @Test
        public void testLogin_UserNotFound() throws Exception {
                // Arrange
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setName("unknownuser");
                loginRequest.setPassword("password");

                when(userService.loginUser("unknownuser", "password")).thenReturn(Optional.empty());

                // Act & Assert
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User not found"));
        }

        @Test
        public void testLogin_InvalidCredentials() throws Exception {
                // Arrange
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setName("testuser");
                loginRequest.setPassword("wrongpassword");

                when(userService.loginUser("testuser", "wrongpassword")).thenReturn(Optional.of(testUser));

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new RuntimeException("Bad credentials"));

                // Act & Assert
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Bad credentials"));
        }

        @Test
        public void testRegister_Success() throws Exception {
                // Arrange
                RegisterUserDTO registerUserDTO = new RegisterUserDTO();
                registerUserDTO.setName("newuser");
                registerUserDTO.setPassword("password");
                registerUserDTO.setRole(UserRole.STUDENT);
                registerUserDTO.setEmail("newuser@example.com");

                User newUser = new User();
                newUser.setId(2L);
                newUser.setName("newuser");
                newUser.setPassword("password");
                newUser.setEmail("newuser@example.com");
                newUser.setRole(UserRole.STUDENT);

                when(userService.registerUser(any(RegisterUserDTO.class))).thenReturn(newUser);

                // Act & Assert
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerUserDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Registration successful"))
                                .andExpect(jsonPath("$.data.name").value("newuser"));
        }

        @Test
        public void testRegister_ExistingUser() throws Exception {
                // Arrange
                RegisterUserDTO registerUserDTO = new RegisterUserDTO();
                registerUserDTO.setName("existinguser");
                registerUserDTO.setPassword("password");
                registerUserDTO.setEmail("existinguser@example.com");
                registerUserDTO.setRole(UserRole.STUDENT);

                when(userService.registerUser(any(RegisterUserDTO.class)))
                                .thenThrow(new RuntimeException("User already exists"));

                // Act & Assert
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerUserDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User already exists"));
        }
}