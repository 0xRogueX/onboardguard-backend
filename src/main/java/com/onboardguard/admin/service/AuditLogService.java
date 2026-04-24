package com.onboardguard.admin.service;

import com.onboardguard.admin.dto.AuditLogDto;
import com.onboardguard.shared.common.events.BusinessLogEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AuditLogService {
    @Async
    @EventListener
    @Transactional
    void handleBusinessLogEvent(BusinessLogEvent event);

    @Transactional(readOnly = true)
    List<AuditLogDto> getEntityHistory(String entityType, Long entityId);
}
