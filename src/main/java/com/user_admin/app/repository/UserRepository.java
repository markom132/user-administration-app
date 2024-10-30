package com.user_admin.app.repository;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Provides methods for performing CRUD operations and custom queries related to users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user to find
     * @return an {@link Optional} containing the found {@link User}, or empty if no user is found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds users based on optional filtering criteria such as status, first name, last name, and email.
     *
     * @param status    the status of the users to filter by (optional)
     * @param firstName the first name of the users to filter by (optional)
     * @param lastName  the last name of the users to filter by (optional)
     * @param email     the email of the users to filter by (optional)
     * @return an {@link Optional} containing a list of {@link User} entities matching the criteria, or empty if none are found
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:firstName IS NULL OR u.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR u.lastName LIKE %:lastName%) AND " +
            "(:email IS NULL OR u.email LIKE %:email%)")
    Optional<List<User>> findByFilters(UserStatus status, String firstName, String lastName, String email);
}
