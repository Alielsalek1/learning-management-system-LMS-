package com.main.lms.enums;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
    ADMIN,
    INSTRUCTOR,
    STUDENT;

    public List<GrantedAuthority> getAuthorities() {
        switch (this) {
            case ADMIN:
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case INSTRUCTOR:
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"));
            case STUDENT:
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"));
            default:
                return Collections.emptyList();
        }
    }
}
