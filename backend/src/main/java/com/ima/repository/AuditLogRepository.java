package com.ima.repository;

import com.ima.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByUsername(String username);
    List<AuditLog> findByScreenId(String screenId);
    List<AuditLog> findByAction(String action);
}
