package com.onboardguard.candidate.mapper;

import com.onboardguard.auth.entity.AppUser;
import com.onboardguard.candidate.dto.request.PersonalDetailsRequestDto;
import com.onboardguard.candidate.dto.request.ProfessionalDetailsRequestDto;
import com.onboardguard.candidate.dto.response.CandidateStatusResponseDto;
import com.onboardguard.candidate.dto.response.DocumentResponseDto;
import com.onboardguard.candidate.entity.Candidate;
import com.onboardguard.candidate.entity.CandidateDocument;
import com.onboardguard.candidate.entity.CandidatePersonalDetail;
import com.onboardguard.candidate.entity.CandidateProfessionalDetail;
import com.onboardguard.candidate.enums.DocumentStatus;
import com.onboardguard.candidate.enums.DocumentType;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CandidateMapper {

    // Update existing Personal Detail entity with DTO data
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "fullNameNormalized", ignore = true) // Handled in service layer
    void updatePersonalDetailFromDto(PersonalDetailsRequestDto dto, @MappingTarget CandidatePersonalDetail entity);

    // Update existing Professional Detail entity with DTO data
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    void updateProfessionalDetailFromDto(ProfessionalDetailsRequestDto dto, @MappingTarget CandidateProfessionalDetail entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "candidateType", constant = "EMPLOYEE")
    @Mapping(target = "onboardingStatus", constant = "REGISTERED")
    Candidate toEntity(AppUser user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", source = "candidate")
    CandidateProfessionalDetail initProfessionalDetail(Candidate candidate);

    @Mapping(target = "isSubmitted", expression = "java(candidate.getFormSubmittedAt() != null)")
    CandidateStatusResponseDto toStatusDto(Candidate candidate);

    // PERSONAL DETAILS - CREATE
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", source = "candidate")
    @Mapping(target = "fullNameNormalized", ignore = true) // handled in service
    CandidatePersonalDetail toPersonalDetailEntity(PersonalDetailsRequestDto dto, Candidate candidate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", source = "candidate")
    @Mapping(target = "documentType", source = "documentType")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "uploadedAt", source = "uploadedAt")
    CandidateDocument initDocument(Candidate candidate, DocumentType documentType, Instant uploadedAt);

    // Map Document Entity to Response DTO with presigned URL
    @Mapping(target = "fileUrl", source = "presignedUrl")
    DocumentResponseDto toDocumentDto(CandidateDocument entity, String presignedUrl);

}