package com.onboardguard.shared.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@ConditionalOnProperty(
        prefix = "onboardguard.seeder.cloudinary-folder",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class CloudinaryFolderSeeder implements CommandLineRunner {

    private final EvidenceSeederService evidenceSeederService;

    @Override
    public void run(String... args) {

        log.info("Starting Cloudinary Evidence Seeder...");
        evidenceSeederService.processAllEvidence();
        log.info("Seeder completed successfully.");
    }
}