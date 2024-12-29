package com.main.lms.controller;

import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.Notification;
import com.main.lms.entities.User;
import com.main.lms.enums.NotificationFlag;
import com.main.lms.exceptions.NotFoundRunTimeException;
import com.main.lms.exceptions.UnauthorizedException;
import com.main.lms.services.NotificationService;
import com.main.lms.utility.SessionIdUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private SessionIdUtility sessionIdUtility;

    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        userDetails = new CustomUserDetails(user);
    }

    @Test
    public void testGetNotifications_Success() throws Exception {
        // Mock the session utility to return the user
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);

        // Create mock notifications
        Notification notification1 = new Notification();
        notification1.setNotificationId(1L);
        notification1.setNotificationMessage("Message 1");
        notification1.setIsRead(false);

        Notification notification2 = new Notification();
        notification2.setNotificationId(2L);
        notification2.setNotificationMessage("Message 2");
        notification2.setIsRead(false);

        List<Notification> notifications = Stream.of(notification1, notification2).collect(Collectors.toList());

        // Mock the notification service
        when(notificationService.GetNotifications(eq(user.getId()), eq(NotificationFlag.All))).thenReturn(notifications);

        // Perform the GET request
        mockMvc.perform(get("/notifications/{flag}", "All")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].message").value("Mes..."))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].message").value("Mes..."));
    }

    @Test
    public void testGetNotifications_InternalServerError() throws Exception {
        // Mock the session utility to return the user
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);

        // Mock the notification service to throw an exception
        when(notificationService.GetNotifications(eq(user.getId()), eq(NotificationFlag.All)))
                .thenThrow(new RuntimeException("Database error"));

        // Perform the GET request
        mockMvc.perform(get("/notifications/{flag}", "All")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Database error"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testGetNotification_Success() throws Exception {
        Long notificationId = 1L;

        // Mock the session utility to return the user
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);

        // Create a mock notification
        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setNotificationMessage("Detailed message");
        notification.setIsRead(false);

        // Mock the notification service
        when(notificationService.ReadNotification(eq(user.getId()), eq(notificationId))).thenReturn(notification);

        // Perform the GET request
        mockMvc.perform(get("/notifications/id/{id}", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(notificationId))
                .andExpect(jsonPath("$.data.message").value("Detailed message"));
    }

    @Test
    public void testGetNotification_Unauthorized() throws Exception {
        Long notificationId = 1L;

        // Mock the session utility to return the user
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);

        // Mock the notification service to throw UnauthorizedException
        doThrow(new UnauthorizedException("UNAUTHORIZED"))
                .when(notificationService).ReadNotification(eq(user.getId()), eq(notificationId));

        // Perform the GET request
        mockMvc.perform(get("/notifications/id/{id}", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testGetNotification_NotFound() throws Exception {
        Long notificationId = 1L;

        // Mock the session utility to return the user
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);

        // Mock the notification service to throw NotFoundRunTimeException
        doThrow(new NotFoundRunTimeException("Notification not found"))
                .when(notificationService).ReadNotification(eq(user.getId()), eq(notificationId));

        // Perform the GET request
        mockMvc.perform(get("/notifications/id/{id}", notificationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Notification not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    public void testGetNotifications_InvalidFlag() throws Exception {
        // Mock the session utility to return the user
        when(sessionIdUtility.getUserFromSessionId()).thenReturn(userDetails);

        // Perform the GET request with an invalid flag
        mockMvc.perform(get("/notifications/{flag}", "InvalidFlag")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}