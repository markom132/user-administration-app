package com.user_admin.app.repository;

import com.user_admin.app.model.UserChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChangeLogRepository extends JpaRepository<UserChangeLog, Long> {
    Optional<UserChangeLog> findByUserId(Long userId);
    List<UserChangeLog> findByChangedByLastName(String lastName);
}
