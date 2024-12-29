package com.main.lms.utility;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.main.lms.entities.CustomUserDetails;

@Service
public class SessionIdUtility {
    public CustomUserDetails getUserFromSessionId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            throw new ClassCastException("User is not authenticated");
        }
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        return user;
    }
}
