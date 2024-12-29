package com.main.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.lms.dtos.RegisterUserDTO;
import com.main.lms.dtos.UpdateUserDTO;
import com.main.lms.dtos.UserResponseDTO;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;

    @BeforeEach
    public void setUp() {
        // Initialize test user or other setup tasks if necessary
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setRole(UserRole.STUDENT);
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        RegisterUserDTO createUserDTO = new RegisterUserDTO();
        createUserDTO.setName("newuser");
        createUserDTO.setEmail("newuser@example.com");
        createUserDTO.setPassword("password");
        createUserDTO.setRole(UserRole.STUDENT);

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setName("newuser");
        responseDTO.setEmail("newuser@example.com");
        responseDTO.setRole(UserRole.STUDENT.name());

        User user = new User();
        user.setId(2L);
        user.setName("newuser");
        user.setEmail("newuser@example.com");
        user.setRole(UserRole.STUDENT);
        user.setPassword("password");
        when(userService.createUser(any(RegisterUserDTO.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.name").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    public void testCreateUser_ExistingUser() throws Exception {
        RegisterUserDTO createUserDTO = new RegisterUserDTO();
        createUserDTO.setName("existinguser");
        createUserDTO.setEmail("existinguser@example.com");
        createUserDTO.setPassword("password");
        createUserDTO.setRole(UserRole.STUDENT);

        when(userService.createUser(any(RegisterUserDTO.class)))
                .thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testGetUserById_Success() throws Exception {
        Long userId = 1L;

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setName("testuser");
        responseDTO.setEmail("testuser@example.com");
        responseDTO.setRole(UserRole.STUDENT.name());
        User user = new User();
        user.setId(userId);
        user.setName("testuser");
        user.setEmail("testuser@example.com");
        user.setRole(UserRole.STUDENT);
        user.setPassword("password");

        when(userService.findUserById(eq(userId))).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User found"))
                .andExpect(jsonPath("$.data.name").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        Long userId = 99L;

        when(userService.findUserById(eq(userId))).thenReturn(null);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        Long userId = 1L;
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("updateduser");
        updateUserDTO.setEmail("updateduser@example.com");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setName("updateduser");
        responseDTO.setEmail("updateduser@example.com");
        responseDTO.setRole(UserRole.STUDENT.name());

        User user = new User();
        user.setId(userId);
        user.setName("updateduser");
        user.setEmail("updateduser@example.com");
        user.setRole(UserRole.STUDENT);
        user.setPassword("password");

        when(userService.updateUser(eq(userId), any(UpdateUserDTO.class))).thenReturn(Optional.of(user));

        mockMvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.name").value("updateduser"))
                .andExpect(jsonPath("$.data.email").value("updateduser@example.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    public void testUpdateUser_NotFound() throws Exception {
        Long userId = 99L;
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("nonexistentuser");
        updateUserDTO.setEmail("nonexistent@example.com");

        when(userService.updateUser(eq(userId), any(UpdateUserDTO.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

}