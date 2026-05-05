package com.onboardguard.admin.service.impl;

import com.onboardguard.admin.dto.AuditLogDto;
import com.onboardguard.admin.entity.AuditLog;
import com.onboardguard.admin.mapper.AuditLogMapper;
import com.onboardguard.admin.repository.AuditLogRepository;
import com.onboardguard.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.onboardguard.shared.common.events.BusinessLogEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @EventListener
    @Transactional
    @Override
    public void handleBusinessLogEvent(BusinessLogEvent event) {
        log.info(">>>> [AUDIT] Received BusinessLogEvent for {} with action {}", event.entityType(), event.action());

        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(event.entityType())
                    .entityId(event.entityId())
                    .action(event.action())
                    .oldStatus(event.oldStatus())
                    .newStatus(event.newStatus())
                    .performedBy(event.performedBy())
                    .actorRole(event.actorRole())
                    .remarks(event.remarks())
                    .createdAt(LocalDateTime.now())
                    .build();

            AuditLog saved = auditLogRepository.save(auditLog);
            log.info(">>>> [AUDIT] Successfully saved AuditLog ID: {}", saved.getId());
        } catch (Exception e) {
            log.error(">>>> [AUDIT] Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * 2. THE UI PROVIDER: Returns the timeline for a specific candidate/entry.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDto> getEntityHistory(String entityType, Long entityId) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(auditLogMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllAuditLogs(String entityType, String action, Long performedBy, Pageable pageable) {
        String type = (entityType != null && !entityType.trim().isEmpty()) ? entityType : null;
        String act = (action != null && !action.trim().isEmpty()) ? action : null;

        return auditLogRepository.findWithFilters(type, act, performedBy, pageable)
                .map(auditLogMapper::toDto);
    }
}