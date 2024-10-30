package com.user_admin.app.repository;

import com.user_admin.app.model.UserChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link UserChangeLog} entities.
 * Provides methods for performing CRUD operations and custom queries related to user changeLog
 */
@Repository
public interface UserChangeLogRepository extends JpaRepository<UserChangeLog, Long> {

    /**
     * Finds all {@link UserChangeLog} entries associated with a specific user by their ID.
     *
     * @param userId the ID of the user for whom to find logs
     * @return an {@link Optional} containing a list of {@link UserChangeLog} entries for a specific user, or empty if none are found
     */
    Optional<List<UserChangeLog>> findByUserId(Long userId);
}
