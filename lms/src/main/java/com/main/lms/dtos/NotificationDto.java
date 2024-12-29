package com.main.lms.dtos;

import com.main.lms.entities.Notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDto {
    private Long id;
    private String message;
    private boolean isRead;

    public NotificationDto(Notification notification) {
        this.id = notification.getNotificationId();
        this.message = notification.getNotificationMessage();
        this.isRead = notification.getIsRead();
    }
}
