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

    @Query(value = "SELECT * FROM business_audit_logs WHERE " +
            "(:entityType IS NULL OR entity_type = :entityType) AND " +
            "(:action IS NULL OR action = :action) AND " +
            "(:performedBy IS NULL OR performed_by = :performedBy) " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM business_audit_logs WHERE " +
                    "(:entityType IS NULL OR entity_type = :entityType) AND " +
                    "(:action IS NULL OR action = :action) AND " +
                    "(:performedBy IS NULL OR performed_by = :performedBy)",
            nativeQuery = true)
    Page<AuditLog> findWithFilters(
            @Param("entityType") String entityType,
            @Param("action") String action,
            @Param("performedBy") Long performedBy,
            Pageable pageable);
}