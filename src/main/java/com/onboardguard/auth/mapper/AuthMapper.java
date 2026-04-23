package com.onboardguard.auth.mapper;

import com.onboardguard.auth.dto.request.CreateOfficerDto;
import com.onboardguard.auth.dto.request.RegisterCandidateDto;
import com.onboardguard.auth.dto.response.CandidateLoginResponseDto;
import com.onboardguard.auth.dto.response.StaffLoginResponseDto;
import com.onboardguard.auth.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    // ========================================================================
    // REQUEST DTO -> ENTITY MAPPINGS
    // ========================================================================

    @Mapping(target = "passwordHash", ignore = true) // Handled securely via encoder in service
    @Mapping(target = "username", ignore = true)     // Generated dynamically in service
    @Mapping(target = "role", constant = "ROLE_CANDIDATE")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "locked", constant = "false")
    AppUser toCandidateEntity(RegisterCandidateDto dto);

    @Mapping(target = "passwordHash", ignore = true) // Generated securely via credential generator
    @Mapping(target = "username", ignore = true)     // Generated dynamically in service
    @Mapping(target = "role", constant = "ROLE_OFFICER_L1")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "locked", constant = "false")
    @Mapping(target = "createdBy", ignore = true)    // Assigned from SecurityContext
    AppUser toOfficerEntity(CreateOfficerDto dto);


    // ========================================================================
    // ENTITY -> RESPONSE DTO MAPPINGS
    // ========================================================================

    /**
     * Maps AppUser to CandidateLoginResponseDto.
     * Takes the JWT token and expiration as additional parameters since they don't exist on the entity.
     */
    @Mapping(target = "token", source = "token")
    @Mapping(target = "expiresInSeconds", source = "expiresInSeconds")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    CandidateLoginResponseDto toCandidateResponse(AppUser user, String token, long expiresInSeconds);

    /**
     * Maps AppUser to StaffLoginResponseDto.
     * Takes the JWT token, roleCode, and expiration as additional parameters.
     */
    @Mapping(target = "token", source = "token")
    @Mapping(target = "roleCode", source = "roleCode")
    @Mapping(target = "expiresInSeconds", source = "expiresInSeconds")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user.fullName")
    StaffLoginResponseDto toStaffResponse(AppUser user, String token, String roleCode, long expiresInSeconds);
}