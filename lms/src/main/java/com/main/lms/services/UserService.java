package com.main.lms.services;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.main.lms.entities.CustomUserDetails;
import com.main.lms.entities.User;
import com.main.lms.enums.UserRole;
import com.main.lms.dtos.RegisterUserDTO;
import com.main.lms.dtos.UpdateUserDTO;
import com.main.lms.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    public Optional<User> loginUser(String name, String password) {
        Optional<User> userOptional = userRepository.findByName(name);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Optional.empty();
        }
        return userOptional;
    }

    public User registerUser(RegisterUserDTO requestBody) {
        if (userRepository.findByEmail(requestBody.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(requestBody.getName());
        user.setEmail(requestBody.getEmail());
        user.setPassword(passwordEncoder.encode(requestBody.getPassword()));
        user.setRole(UserRole.STUDENT);
        return userRepository.save(user);
    }
    public User createUser(RegisterUserDTO userDto) {
        if(userRepository.findByEmail(userDto.getEmail()).isPresent()){
            throw new RuntimeException("Email already in use");
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole());
        return userRepository.save(user);
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> updateUser(Long id, UpdateUserDTO userDetails) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User existingUser = userOptional.get();
        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        
        return Optional.of(userRepository.save(existingUser)); 
    }

    public void deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByName(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(userOptional.get());
    }
}
