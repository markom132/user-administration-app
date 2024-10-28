package com.user_admin.app.repository;

import com.user_admin.app.model.RequestResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestResponseLogRepository extends JpaRepository<RequestResponseLog, Long> {
    List<RequestResponseLog> findByMethod(String method);
    List<RequestResponseLog> findByEndpoint(String endpoint);
    List<RequestResponseLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<RequestResponseLog> findByStatusCode(int statusCode);

    RequestResponseLog findTopByEndpointAndMethodOrderByTimestampDesc(String endpoint, String method);

    @Query("SELECT r FROM RequestResponseLog r " +
            "WHERE (:endpoint IS NULL OR r.endpoint = :endpoint) " +
            "AND (:method IS NULL OR r.method = :method) " +
            "AND (:statusCode IS NULL OR r.statusCode = :statusCode)")
    List<RequestResponseLog> findByCriteria(
            @Param("endpoint") String endpoint,
            @Param("method") String method,
            @Param("statusCode") Integer statusCode);
}
