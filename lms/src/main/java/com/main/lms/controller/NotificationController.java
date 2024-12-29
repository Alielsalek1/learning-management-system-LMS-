package com.main.lms.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main.lms.dtos.ApiResponse;
import com.main.lms.dtos.NotificationDto;
import com.main.lms.enums.NotificationFlag;
import com.main.lms.exceptions.NotFoundRunTimeException;
import com.main.lms.exceptions.UnauthorizedException;
import com.main.lms.services.NotificationService;
import com.main.lms.utility.SessionIdUtility;

import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SessionIdUtility sessionIdUtility;

    // All,Unread,Read
    @GetMapping("/{flag}")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> GetNotifications(@PathVariable NotificationFlag flag) {
        try {
            var userDetails = sessionIdUtility.getUserFromSessionId();
            Long userId = userDetails.getUser().getId();
            ApiResponse<List<NotificationDto>> response = new ApiResponse<List<NotificationDto>>();
            List<NotificationDto> notifications = notificationService
                    .GetNotifications(userId, flag).stream()
                    .peek(notification -> notification.setNotificationMessage(
                            notification.getNotificationMessage().substring(0, 3) + "..."))
                    .map(NotificationDto::new)
                    .toList();
            response.setSuccess(true);
            response.setData(notifications);
            response.setMessage("Success");
            return ResponseEntity.ok(response);
        } catch (Exception exception) {
            var response = new ApiResponse<List<NotificationDto>>();
            response.setSuccess(false);
            response.setMessage(exception.getMessage());
            response.setData(new LinkedList<NotificationDto>());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<NotificationDto>> GetNotification(@PathVariable Long id) {
        ApiResponse<NotificationDto> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        try {
            var userDetails = sessionIdUtility.getUserFromSessionId();
            Long userId = userDetails.getUser().getId();
            var notification = notificationService.ReadNotification(userId, id);
            var notificationDto = new NotificationDto(notification);
            response.setData(notificationDto);
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException exception) {
            response.setSuccess(false);
            response.setMessage("UNAUTHORIZED");
            response.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (NotFoundRunTimeException exception) {
            response.setSuccess(false);
            response.setMessage(exception.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
