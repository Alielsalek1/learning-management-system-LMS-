package com.main.lms.entities;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails{
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRole().getAuthorities();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // You can add expiration logic here
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Add lock logic if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add credentials expiration logic if needed
    }

    @Override
    public boolean isEnabled() {
        return true; // You can add logic for disabling the user here
    }


}
