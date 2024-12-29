package com.main.lms.services;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.main.lms.dtos.RegisterUserDTO;
import com.main.lms.dtos.UpdateUserDTO;
import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.repositories.UserRepository;
import com.main.lms.services.UserService;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterUserDTO registerUserDTO;
    private UpdateUserDTO updateUserDTO;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.STUDENT);

        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setName("jane_doe");
        registerUserDTO.setEmail("jane@example.com");
        registerUserDTO.setPassword("password123");
        registerUserDTO.setRole(UserRole.STUDENT);

        updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setName("john_updated");
        updateUserDTO.setEmail("john_updated@example.com");
        updateUserDTO.setPassword("newpassword123");
    }

    @Test
    public void testLoginUser_Success() {
        String name = "john_doe";
        String password = "password123";
        when(userRepository.findByName(name)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        Optional<User> result = userService.loginUser(name, password);

        assertTrue(result.isPresent());
        verify(userRepository, times(1)).findByName(name);
        verify(passwordEncoder, times(1)).matches(password, user.getPassword());
    }

    @Test
    public void testLoginUser_UserNotFound() {
        String name = "nonexistent_user";
        String password = "password123";
        when(userRepository.findByName(name)).thenReturn(Optional.empty());

        Optional<User> result = userService.loginUser(name, password);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByName(name);
        verify(passwordEncoder, times(0)).matches(anyString(), anyString());
    }

    @Test
    public void testLoginUser_IncorrectPassword() {
        String name = "john_doe";
        String password = "wrongPassword";
        when(userRepository.findByName(name)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        Optional<User> result = userService.loginUser(name, password);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByName(name);
        verify(passwordEncoder, times(1)).matches(password, user.getPassword());
    }

    @Test
    public void testRegisterUser_Success() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simulate generated ID
            return savedUser;
        });

        User registeredUser = userService.registerUser(registerUserDTO);

        assertNotNull(registeredUser);
        assertEquals(1L, registeredUser.getId());
        assertEquals(registerUserDTO.getName(), registeredUser.getName());
        assertEquals(registerUserDTO.getEmail(), registeredUser.getEmail());
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals(UserRole.STUDENT, registeredUser.getRole());
        verify(userRepository, times(1)).findByEmail(registerUserDTO.getEmail());
        verify(passwordEncoder, times(1)).encode(registerUserDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUser_EmailAlreadyInUse() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registerUserDTO);
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(registerUserDTO.getEmail());
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void testCreateUser_Success() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L); // Simulate generated ID
            return savedUser;
        });

        User createdUser = userService.createUser(registerUserDTO);

        assertNotNull(createdUser);
        assertEquals(2L, createdUser.getId());
        assertEquals(registerUserDTO.getName(), createdUser.getName());
        assertEquals(registerUserDTO.getEmail(), createdUser.getEmail());
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals(registerUserDTO.getRole(), createdUser.getRole());
        verify(userRepository, times(1)).findByEmail(registerUserDTO.getEmail());
        verify(passwordEncoder, times(1)).encode(registerUserDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUser_EmailAlreadyInUse() {
        when(userRepository.findByEmail(registerUserDTO.getEmail())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(registerUserDTO);
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(registerUserDTO.getEmail());
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void testFindUserById_UserFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindUserById_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(2L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    public void testFindUserByUsername_UserFound() {
        when(userRepository.findByName("john_doe")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByUsername("john_doe");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository, times(1)).findByName("john_doe");
    }

    @Test
    public void testFindUserByUsername_UserNotFound() {
        when(userRepository.findByName("unknown_user")).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserByUsername("unknown_user");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByName("unknown_user");
    }

    @Test
    public void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userService.updateUser(1L, updateUserDTO);

        assertTrue(result.isPresent());
        User updatedUser = result.get();
        assertEquals("john_updated", updatedUser.getName());
        assertEquals("john_updated@example.com", updatedUser.getEmail());
        assertEquals("newEncodedPassword", updatedUser.getPassword());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode(updateUserDTO.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(2L, updateUserDTO);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void testUpdateUser_PasswordNullOrEmpty() {
        updateUserDTO.setPassword(null); // or set it to an empty string ""
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = userService.updateUser(1L, updateUserDTO);

        assertTrue(result.isPresent());
        User updatedUser = result.get();
        assertEquals("john_updated", updatedUser.getName());
        assertEquals("john_updated@example.com", updatedUser.getEmail());
        assertEquals(user.getPassword(), updatedUser.getPassword()); // Password should remain unchanged
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUser_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(2L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(0)).deleteById(anyLong());
    }

    @Test
    public void testLoadUserByUsername_Success() {
        when(userRepository.findByName("john_doe")).thenReturn(Optional.of(user));

        CustomUserDetails userDetails = (CustomUserDetails) userService.loadUserByUsername("john_doe");

        assertNotNull(userDetails);
        assertEquals(user.getName(), userDetails.getUsername());
        verify(userRepository, times(1)).findByName("john_doe");
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByName("unknown_user")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("unknown_user");
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByName("unknown_user");
    }

}