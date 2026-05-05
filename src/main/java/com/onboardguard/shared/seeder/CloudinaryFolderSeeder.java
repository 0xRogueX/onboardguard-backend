package com.onboardguard.shared.seeder;

import com.onboardguard.shared.storage.CloudStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloudinaryFolderSeeder implements CommandLineRunner {

    private final CloudStorageService cloudStorageService;

    // Helper class to hold your exact SQL data
    private record EvidenceRecord(int entryId, int sourceId, String type, String fileName) {}

    @Override
    public void run(String... args) throws Exception {
        boolean runSeeder = false; // Set to false after running once

        if (!runSeeder) return;

        log.info("Starting automated Cloudinary upload with precise SQL data stamping...");

        ClassPathResource resource = new ClassPathResource("compliance-evidence-document-template.jpg");
        byte[] templateBytes;
        try (InputStream is = resource.getInputStream()) {
            templateBytes = is.readAllBytes();
        } catch (Exception e) {
            log.error("Template not found! Place it in src/main/resources/compliance-evidence-document-template.jpg");
            return;
        }

        // EXACT match of your SQL rows
        List<EvidenceRecord> records = List.of(
                new EvidenceRecord(1001, 1, "REGULATORY_ORDER", "un_resolution"),
                new EvidenceRecord(1002, 6, "NEWS_ARTICLE", "wsj_article"),
                new EvidenceRecord(1003, 5, "REGULATORY_ORDER", "rbi_wilful_defaulter_notice"),
                new EvidenceRecord(1004, 5, "COURT_ORDER", "pnb_scam_fir"),
                new EvidenceRecord(1005, 5, "REGULATORY_ORDER", "choksi_ed_attachment"),
                new EvidenceRecord(1006, 7, "INTERNAL_REPORT", "hr_internal_memo_smit"),
                new EvidenceRecord(1007, 7, "INTERNAL_REPORT", "cyber_fraud_internal_report"),
                new EvidenceRecord(1008, 4, "REGULATORY_ORDER", "sebi_debarment_order"),
                new EvidenceRecord(1009, 6, "NEWS_ARTICLE", "pep_news_article"),
                new EvidenceRecord(1010, 3, "COURT_ORDER", "cbi_chargesheet"),
                new EvidenceRecord(1011, 7, "INTERNAL_REPORT", "vendor_blacklisting_memo"),
                new EvidenceRecord(1012, 7, "INTERNAL_REPORT", "employment_fraud_investigation"),
                new EvidenceRecord(1013, 6, "NEWS_ARTICLE", "local_pep_coverage"),
                new EvidenceRecord(1014, 7, "INTERNAL_REPORT", "suspicious_activity_log"),
                new EvidenceRecord(1015, 4, "REGULATORY_ORDER", "sebi_fraud_reference"),
                new EvidenceRecord(1016, 3, "COURT_ORDER", "cbi_kidney_racket_fir"),
                new EvidenceRecord(1017, 3, "COURT_ORDER", "extortion_case_details"),
                new EvidenceRecord(1018, 2, "OTHER", "interpol_red_notice"),
                new EvidenceRecord(1019, 2, "OTHER", "interpol_red_notice_rajan"),
                new EvidenceRecord(1020, 7, "INTERNAL_REPORT", "fake_billing_invoice"),
                new EvidenceRecord(1021, 7, "INTERNAL_REPORT", "job_scam_complaint"),
                new EvidenceRecord(1022, 6, "NEWS_ARTICLE", "political_scandal_news"),
                new EvidenceRecord(1023, 6, "NEWS_ARTICLE", "pep_declaration_form"),
                new EvidenceRecord(1024, 3, "COURT_ORDER", "financial_fraud_cbi_docket"),
                new EvidenceRecord(1025, 4, "REGULATORY_ORDER", "stock_market_scam_sebi"),
                new EvidenceRecord(1026, 3, "COURT_ORDER", "extortion_fir"),
                new EvidenceRecord(1027, 4, "REGULATORY_ORDER", "license_revocation_notice"),
                new EvidenceRecord(1028, 7, "INTERNAL_REPORT", "contract_violation_memo"),
                new EvidenceRecord(1029, 7, "INTERNAL_REPORT", "recruitment_scam_report"),
                new EvidenceRecord(1030, 3, "COURT_ORDER", "organized_crime_chargesheet")
        );

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));

        for (EvidenceRecord record : records) {
            try {
                // 1. Format the text for the image
                String evidenceId = "WLE-" + record.entryId(); // e.g., WLE-1001
                String displayType = record.type().replace("_", " ");
                String sourceName = mapSourceIdToName(record.sourceId()); // Dynamic source mapping
                String refCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                // 2. Build the exact Cloudinary folder path
                String uploadPath = String.format("watchlist/evidence/%d/%s/%s",
                        record.entryId(), record.type(), record.fileName());

                // 3. Stamp image and Upload
                byte[] stampedImage = stampTextOnImage(templateBytes, evidenceId, displayType, sourceName, currentDate, refCode);
                cloudStorageService.uploadBytes(uploadPath, stampedImage, "image/jpeg");

                log.info("Uploaded successfully: {}", uploadPath);
            } catch (Exception e) {
                log.error("Failed on entry: {}", record.entryId(), e);
            }
        }
        log.info("Cloudinary upload complete!");
    }

    // Maps your numeric source_ids to professional display names for the image
    private String mapSourceIdToName(int sourceId) {
        return switch (sourceId) {
            case 1 -> "UNITED NATIONS SEC. COUNCIL";
            case 2 -> "INTERPOL DATABASES";
            case 3 -> "FEDERAL/STATE COURTS & CBI";
            case 4 -> "SECURITIES & EXCHANGE (SEBI)";
            case 5 -> "FINANCIAL REGULATORS (RBI/ED)";
            case 6 -> "GLOBAL NEWS / MEDIA OUTLETS";
            case 7 -> "INTERNAL COMPLIANCE DESK";
            default -> "SYSTEM GENERATED RECORD";
        };
    }

    private byte[] stampTextOnImage(byte[] templateBytes, String id, String type,
                                    String source, String date, String refCode) throws Exception {

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(templateBytes));
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.setColor(new Color(17, 42, 85));

        // -------------------------------------------------------
        // CALIBRATED COORDINATES (template: 1086 x 1448 px)
        // Method: icon circle centers detected at y = 598,674,750,827,904
        // drawString y = icon_center + 7  (text baseline offset for 20pt bold)
        // Vertical separator between labels/values at x = 462
        // Value text starts at x = 520 (separator + 58px padding)
        // -------------------------------------------------------

        int startX = 520;

        g2d.drawString(id,      startX, 605);  // EVIDENCE ID    — icon center y=598
        g2d.drawString(type,    startX, 681);  // EVIDENCE TYPE  — icon center y=674
        g2d.drawString(source,  startX, 757);  // SOURCE         — icon center y=750
        g2d.drawString(date,    startX, 834);  // DATE GENERATED — icon center y=827
        g2d.drawString(refCode, startX, 911);  // REFERENCE CODE — icon center y=904

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}