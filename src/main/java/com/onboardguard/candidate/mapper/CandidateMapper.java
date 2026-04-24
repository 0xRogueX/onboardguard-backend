package com.onboardguard.candidate.mapper;

import com.onboardguard.candidate.dto.request.PersonalDetailsRequestDto;
import com.onboardguard.candidate.dto.request.ProfessionalDetailsRequestDto;
import com.onboardguard.candidate.entity.CandidatePersonalDetail;
import com.onboardguard.candidate.entity.CandidateProfessionalDetail;
import org.mapstruct.*;

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
}