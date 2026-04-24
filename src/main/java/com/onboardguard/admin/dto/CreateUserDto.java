package com.onboardguard.admin.dto;

import com.onboardguard.shared.common.enums.RoleCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;

//    @NotBlank(message = "Full name is required")
//    private String fullName;
//
//    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid (E.164 format)")
//    private String phone;

    @NotNull(message = "Role is required")
    private RoleCode role; // e.g., ROLE_OFFICER_L1, ROLE_OFFICER_L2, ROLE_ADMIN
}