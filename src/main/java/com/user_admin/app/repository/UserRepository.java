package com.user_admin.app.repository;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:firstName IS NULL OR u.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR u.lastName LIKE %:lastName%) AND " +
            "(:email IS NULL OR u.email LIKE %:email%)")
    Optional<List<User>> findByFilters(UserStatus status, String firstName, String lastName, String email);
}
