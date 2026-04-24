package com.onboardguard.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogDto {
    private Long id;
    private String action;
    private String oldStatus;
    private String newStatus;
    private String performedBy;
    private String actorRole;
    private String remarks;
    private LocalDateTime createdAt;
}