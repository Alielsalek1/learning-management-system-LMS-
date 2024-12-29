package com.main.lms.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.main.lms.utility.EmailUtility;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmailUtilityTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailUtility emailUtility;

    @BeforeEach
    public void setUp() {
        // Since 'fromEmail' is injected via @Value, we'll set it manually for the test
        emailUtility.setFromEmail("sender@example.com");
    }

    @Test
    public void testSendEmail_Success() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Capture the SimpleMailMessage that is sent
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailUtility.sendEmail(to, subject, body);

        // Assert
        // Verify that mailSender.send() was called once
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // Get the captured message
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Validate the contents of the message
        assertNotNull(sentMessage);
        assertEquals("sender@example.com", sentMessage.getFrom());
        assertArrayEquals(new String[]{to}, sentMessage.getTo());
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }


    @Test
    public void testSendEmail_WithException() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Simulate an exception when mailSender.send() is called
        doThrow(new RuntimeException("Mail server not available"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                emailUtility.sendEmail(to, subject, body));

        // Verify the exception message
        assertEquals("Mail server not available", exception.getMessage());

        // Verify that mailSender.send() was called once
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendEmail_EmptySubjectAndBody() {
        // Arrange
        String to = "recipient@example.com";
        String subject = "";
        String body = "";

        // Capture the SimpleMailMessage that is sent
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailUtility.sendEmail(to, subject, body);

        // Assert
        // Verify that mailSender.send() was called once
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // Get the captured message
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Validate the contents of the message
        assertNotNull(sentMessage);
        assertEquals("sender@example.com", sentMessage.getFrom());
        assertArrayEquals(new String[]{to}, sentMessage.getTo());
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }
}