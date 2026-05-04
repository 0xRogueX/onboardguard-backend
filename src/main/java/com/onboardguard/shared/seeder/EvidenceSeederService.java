package com.onboardguard.shared.seeder;

import com.onboardguard.shared.storage.CloudStorageService;
import com.onboardguard.watchlist.entity.WatchlistEvidenceDocument;
import com.onboardguard.watchlist.repository.WatchlistEvidenceDocRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceSeederService {

    private final WatchlistEvidenceDocRepository evidenceRepository;
    private final CloudStorageService cloudStorageService;
    private final EvidenceImageGenerator imageGenerator;

    @Transactional(readOnly = true)
    public void processAllEvidence() {
        List<WatchlistEvidenceDocument> documents = evidenceRepository.findAllWithRelations();

        if (documents.isEmpty()) {
            log.info("No watchlist evidence documents found to seed.");
            return;
        }

        String currentDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));

        for (WatchlistEvidenceDocument doc : documents) {
            try {
                String evidenceId = "WLE-" + doc.getEntry().getId();
                String type = doc.getEvidenceType().name().replace("_", " ");
                String source = doc.getSource().getName().toUpperCase();
                String refCode = UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase();

                if (cloudStorageService.exists(doc.getCloudStorageKey(), "image/jpeg")) {
                    log.info("Skipping already seeded evidence: {}", doc.getCloudStorageKey());
                    continue;
                }

                byte[] stampedImage = imageGenerator.generate(
                        evidenceId,
                        type,
                        source,
                        currentDate,
                        refCode
                );

                cloudStorageService.uploadBytes(
                        doc.getCloudStorageKey(),
                        stampedImage,
                        "image/jpeg"
                );

                log.info("Uploaded: {}", doc.getCloudStorageKey());

            } catch (Exception e) {
                log.error("Failed for entryId={}",
                        doc.getEntry().getId(), e);
            }
        }
    }
}