package com.user_admin.app.repository;

import com.user_admin.app.model.RequestResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link RequestResponseLog} entities.
 * Provides methods for performing CRUD operations and custom queries related to request and response logs.
 */
@Repository
public interface RequestResponseLogRepository extends JpaRepository<RequestResponseLog, Long> {

    /**
     * Finds all {@link RequestResponseLog} entries within the specified time range.
     *
     * @param start the start time of the range
     * @param end   the end time of the range
     * @return a lsit of {@link RequestResponseLog} entries within the specified time range
     */
    List<RequestResponseLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds the most recent {@link RequestResponseLog} entry for a given endpoints and method.
     *
     * @param endpoint the endpoint to search for
     * @param method   the HTTP method (e.g., GET, POST) to search for
     * @return the most recent {@link RequestResponseLog} entry matching the given endpoint and method
     */
    RequestResponseLog findTopByEndpointAndMethodOrderByTimestampDesc(String endpoint, String method);

    /**
     * Finds {@link RequestResponseLog} entries based on various criteria.
     *
     * @param endpoint   the endpoint to filter by (optional)
     * @param method     the HTTP method to filter by (optional)
     * @param statusCode the status code to filter by (optional)
     * @return an {@link Optional} containing a list of matching {@link RequestResponseLog} entries, or empty if no matches are found
     */
    @Query("SELECT r FROM RequestResponseLog r " +
            "WHERE (:endpoint IS NULL OR r.endpoint LIKE %:endpoint%) " +
            "AND (:method IS NULL OR r.method LIKE %:method%) " +
            "AND (:statusCode IS NULL OR r.statusCode = :statusCode)")
    Optional<List<RequestResponseLog>> findByCriteria(
            String endpoint,
            String method,
            Integer statusCode);

}
