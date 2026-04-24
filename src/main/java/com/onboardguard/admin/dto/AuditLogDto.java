package com.onboardguard.admin.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AuditLogDto (
     Long id,
     String action,
     String oldStatus,
     String newStatus,
     String performedBy,
     String actorRole,
     String remarks,
     LocalDateTime createdAt
){}