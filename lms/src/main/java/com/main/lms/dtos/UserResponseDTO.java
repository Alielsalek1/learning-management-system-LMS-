package com.main.lms.dtos;

import com.main.lms.entities.User;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserResponseDTO {
    private String name;
    private String email;
    private String role;

    public static UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setName(user.getName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setRole(user.getRole().name());
        return userResponseDTO;
    }
}
