package com.onboardguard.admin.mapper;

import com.onboardguard.admin.dto.AuditLogDto;
import com.onboardguard.shared.common.events.BusinessLogEvent; // Or your AuditLog Entity
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {

    AuditLogDto toDto(Object entity); // Replace 'Object' with your actual AuditLog JPA entity class

}