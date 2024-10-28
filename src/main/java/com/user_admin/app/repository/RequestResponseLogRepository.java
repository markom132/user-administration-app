package com.user_admin.app.repository;

import com.user_admin.app.model.RequestResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestResponseLogRepository extends JpaRepository<RequestResponseLog, Long> {

    List<RequestResponseLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    RequestResponseLog findTopByEndpointAndMethodOrderByTimestampDesc(String endpoint, String method);

    @Query("SELECT r FROM RequestResponseLog r " +
            "WHERE (:endpoint IS NULL OR r.endpoint LIKE %:endpoint%) " +
            "AND (:method IS NULL OR r.method LIKE %:method%) " +
            "AND (:statusCode IS NULL OR r.statusCode = :statusCode)")
    Optional<List<RequestResponseLog>> findByCriteria(
            String endpoint,
            String method,
            Integer statusCode);

}
