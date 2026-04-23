package com.onboardguard.auth.service;

public interface OfficerCredentialGenerator {

    String generateUsername(String fullName);

    String generatePassword();
}