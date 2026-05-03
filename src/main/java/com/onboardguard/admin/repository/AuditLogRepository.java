package com.onboardguard.admin.repository;

import com.onboardguard.admin.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // This perfectly supports the UI "History Timeline" view
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    @Query(value = "SELECT a FROM AuditLog a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:performedBy IS NULL OR a.performedBy = :performedBy) " +
            "ORDER BY a.createdAt DESC",
            countQuery = "SELECT COUNT(a) FROM AuditLog a WHERE " +
                    "(:entityType IS NULL OR a.entityType = :entityType) AND " +
                    "(:action IS NULL OR a.action = :action) AND " +
                    "(:performedBy IS NULL OR a.performedBy = :performedBy)")
    Page<AuditLog> findWithFilters(
            @Param("entityType") String entityType,
            @Param("action") String action,
            @Param("performedBy") Long performedBy,
            Pageable pageable);
}