package com.user_admin.app.repository;

import com.user_admin.app.model.RequestResponseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestResponseLogRepository extends JpaRepository<RequestResponseLog, Long> {
    List<RequestResponseLog> findByMethod(String method);
    List<RequestResponseLog> findByUrl(String url);
    List<RequestResponseLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<RequestResponseLog> findByStatusCode(int statusCode);
}
