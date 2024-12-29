package com.main.lms.repositories;

import com.main.lms.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    @NonNull
    Optional<User> findById(@NonNull Long id);

    void deleteById(@NonNull Long id);

    // Custom update query to update user fields
    // @Modifying
    // @Transactional
    // @Query("UPDATE User u SET u.name = :name, u.email = :email, u.password = :password, u.role = :role WHERE u.id = :id")
    // void updateUser(Long id, String name, String email, String password, UserRole role);
}