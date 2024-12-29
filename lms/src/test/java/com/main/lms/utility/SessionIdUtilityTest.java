package com.main.lms.utility;

import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.utility.SessionIdUtility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionIdUtilityTest {

    @InjectMocks
    private SessionIdUtility sessionIdUtility;

    @Mock
    private SecurityContext securityContext;

    @Test
    public void testGetUserFromSessionId_UserAuthenticated() {
        // Arrange
        // Create a User object with necessary properties
        User user = new User();
        user.setName("TestUser");
        user.setPassword("password");
        user.setRole(UserRole.STUDENT); // Ensure UserRole is properly setup
        
        // Create CustomUserDetails with the User object
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Mock the SecurityContextHolder to return our mocked SecurityContext
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Act
            CustomUserDetails result = sessionIdUtility.getUserFromSessionId();

            // Assert
            assertNotNull(result);
            assertEquals(userDetails, result);
        }
    }

    @Test
    public void testGetUserFromSessionId_NoAuthentication() {
        // Arrange: SecurityContextHolder returns null authentication

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            // Act & Assert
            ClassCastException exception = assertThrows(ClassCastException.class, () ->
                    sessionIdUtility.getUserFromSessionId());

            assertEquals("User is not authenticated", exception.getMessage());
        }
    }

    @Test
    public void testGetUserFromSessionId_NotAuthenticated() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Mock the SecurityContextHolder

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Act & Assert
            ClassCastException exception = assertThrows(ClassCastException.class, () ->
                    sessionIdUtility.getUserFromSessionId());

            assertEquals("User is not authenticated", exception.getMessage());
        }
    }

    @Test
    public void testGetUserFromSessionId_PrincipalNotCustomUserDetails() {
        // Arrange
        String principal = "NotCustomUserDetails";
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        // Mock the SecurityContextHolder

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Act & Assert
            assertThrows(ClassCastException.class, () ->
                    sessionIdUtility.getUserFromSessionId());
        }
    }
}