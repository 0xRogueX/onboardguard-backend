package com.onboardguard.shared.seeder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
@Slf4j
public class EvidenceImageGenerator {

    private static final String TEMPLATE_PATH =
            "compliance-evidence-document-template.jpg";

    public byte[] generate(String id, String type,
                           String source, String date,
                           String refCode) throws Exception {

        BufferedImage image = loadTemplate();
        Graphics2D g2d = image.createGraphics();

        applyQualityRendering(g2d);
        applyFontAndColor(g2d);

        int startX = 520;

        g2d.drawString(id,      startX, 605);
        g2d.drawString(type,    startX, 681);
        g2d.drawString(source,  startX, 757);
        g2d.drawString(date,    startX, 834);
        g2d.drawString(refCode, startX, 911);

        g2d.dispose();

        return convertToBytes(image);
    }

    private BufferedImage loadTemplate() throws Exception {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        try (InputStream is = resource.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IllegalStateException("Unable to read evidence template image: " + TEMPLATE_PATH);
            }
            return image;
        }
    }

    private void applyQualityRendering(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private void applyFontAndColor(Graphics2D g2d) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2d.setColor(new Color(17, 42, 85));
    }

    private byte[] convertToBytes(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpg", baos)) {
            throw new IllegalStateException("Failed to encode evidence image as JPG");
        }
        return baos.toByteArray();
    }
}