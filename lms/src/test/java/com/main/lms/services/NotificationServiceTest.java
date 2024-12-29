package com.main.lms.services;


import com.main.lms.entities.Notification;
import com.main.lms.entities.User;
import com.main.lms.exceptions.UserNotFoundException;
import com.main.lms.repositories.NotificationRepository;
import com.main.lms.repositories.UserRepository;
import com.main.lms.services.NotificationService;
import com.main.lms.utility.EmailUtility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailUtility emailUtility;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    public void setUp() {
        // Initialize a sample user
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
    }

    @Test
    public void testNotifyUser_Success() throws UserNotFoundException {
        // Arrange
        long userId = user.getId();
        String message = "Test notification message";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        notificationService.notifyUser(userId, message);

        // Assert
        // Verify that userRepository.findById was called
        verify(userRepository, times(1)).findById(userId);

        // Verify that notificationRepository.save was called with correct Notification
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getUser().equals(user) &&
                notification.getNotificationMessage().equals(message) &&
                notification.getIsRead() == false
        ));

        // Since emailUtility.sendEmail is commented out, verify it was not called
        verifyNoInteractions(emailUtility);
    }

    @Test
    public void testNotifyUser_UserNotFound() {
        // Arrange
        long userId = 2L; // Non-existent user ID
        String message = "Test notification message";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            notificationService.notifyUser(userId, message);
        });

        assertEquals("User not found", exception.getMessage());

        // Verify that userRepository.findById was called
        verify(userRepository, times(1)).findById(userId);

        // Verify that notificationRepository.save was never called
        verify(notificationRepository, never()).save(any(Notification.class));

        // Since emailUtility.sendEmail is commented out, verify it was not called
        verifyNoInteractions(emailUtility);
    }
}