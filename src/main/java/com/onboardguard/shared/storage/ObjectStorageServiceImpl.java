package com.onboardguard.shared.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Primary
@Service
@Slf4j
public class ObjectStorageServiceImpl implements CloudStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public ObjectStorageServiceImpl(
            S3Client s3Client,
            S3Presigner s3Presigner,
            // UPDATED to match your YAML
            @Value("${app.storage.bucket:dummy-bucket}") String bucket) {

        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    @Override
    public String upload(String storageKey, MultipartFile file) {
        log.debug("R2 upload: bucket={} key={} size={}B contentType={}",
                bucket, storageKey, file.getSize(), file.getContentType());
        try {
            byte[] bytes = file.getBytes();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .contentType(file.getContentType())
                    .contentLength((long) bytes.length)
                    // REMOVED: .serverSideEncryption("AES256")
                    // Cloudflare R2 encrypts everything at rest by default.
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            log.debug("R2 upload complete: key={}", storageKey);
            return storageKey;

        } catch (IOException e) {
            throw new CloudStorageException(
                    "Failed to read bytes from uploaded file", storageKey, e);
        } catch (Exception e) {
            throw new CloudStorageException(
                    "R2 upload failed", storageKey, e);
        }
    }

    @Override
    public String uploadBytes(String storageKey, byte[] content, String contentType) {
        log.debug("R2 uploadBytes: bucket={} key={} size={}B contentType={}",
                bucket, storageKey, content.length, contentType);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .contentType(contentType)
                    .contentLength((long) content.length)
                    // REMOVED: .serverSideEncryption("AES256")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.debug("R2 uploadBytes complete: key={}", storageKey);
            return storageKey;

        } catch (Exception e) {
            throw new CloudStorageException(
                    "R2 uploadBytes failed", storageKey, e);
        }
    }

    @Override
    public String generatePresignedUrl(String storageKey, Duration expiry) {
        log.debug("R2 presign: bucket={} key={} expiryMinutes={}",
                bucket, storageKey, expiry.toMinutes());
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(r -> r
                            .bucket(bucket)
                            .key(storageKey))
                    .build();

            PresignedGetObjectRequest presignedRequest =
                    s3Presigner.presignGetObject(presignRequest);

            String url = presignedRequest.url().toString();
            log.debug("R2 presign complete: key={} urlLength={}", storageKey, url.length());
            return url;

        } catch (Exception e) {
            throw new CloudStorageException(
                    "Failed to generate pre-signed URL", storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        log.debug("R2 delete: bucket={} key={}", bucket, storageKey);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build());
            log.debug("R2 delete complete: key={}", storageKey);
        } catch (Exception e) {
            throw new CloudStorageException(
                    "R2 delete failed", storageKey, e);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        log.debug("R2 exists check: bucket={} key={}", bucket, storageKey);
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new CloudStorageException(
                    "R2 existence check failed", storageKey, e);
        }
    }
}