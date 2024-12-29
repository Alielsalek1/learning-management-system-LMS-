package com.main.lms.services;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.main.lms.entities.Notification;
import com.main.lms.entities.User;
import com.main.lms.enums.NotificationFlag;
import com.main.lms.exceptions.NotFoundRunTimeException;
import com.main.lms.exceptions.UnauthorizedException;
import com.main.lms.exceptions.UserNotFoundException;
import com.main.lms.repositories.NotificationRepository;
import com.main.lms.repositories.UserRepository;
import com.main.lms.utility.EmailUtility;

import lombok.RequiredArgsConstructor;

@Service
@RequestScope
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailUtility emailUtility;

    public void notifyUser(long userId, String message) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Notification notification = new Notification();
        notification.setIsRead(false);
        notification.setNotificationMessage(message);
        notification.setUser(user);
        notificationRepository.save(notification);
        try {
            // emailUtility.sendEmail(user.getEmail(), "Lms Notification", message);
        } catch (Exception e) {
            System.out.println("Email not sent");
        }
    }

    public List<Notification> GetNotifications(Long userId, NotificationFlag flag) {
        List<Notification> notifications;
        switch (flag) {
            case All:
                notifications = notificationRepository.findByUserId(userId);
                break;
            case Read:
                notifications = notificationRepository.findByUserIdAndIsRead(userId, true);
                break;
            case Unread:
                notifications = notificationRepository.findByUserIdAndIsRead(userId, true);
                break;
            default:
                notifications = new LinkedList<>();
                break;
        }
        return notifications;
    }

    public Notification ReadNotification(Long userId, Long notificationId)
            throws UnauthorizedException, NotFoundRunTimeException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundRunTimeException("null"));
        if (notification.getUser().getId() != userId)
            throw new UnauthorizedException("invalid notification id");
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        return notification;
    }
}
