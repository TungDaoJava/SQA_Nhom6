package com.routine.service;

import com.routine.config.R2Config;
import com.routine.dto.UploadDtos;
import com.routine.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2UploadService {

    private static final long MAX_UPLOAD_SIZE = 10L * 1024 * 1024;
    private static final Duration PRESIGNED_URL_TTL = Duration.ofMinutes(5);
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/avif", "avif");

    private final R2Config r2Config;

    public UploadDtos.PresignResponse createProductImageUpload(UploadDtos.PresignRequest request) {
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        String extension = IMAGE_EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new BadRequestException("Chỉ hỗ trợ ảnh JPEG, PNG, WebP hoặc AVIF.");
        }
        if (request.size() > MAX_UPLOAD_SIZE) {
            throw new BadRequestException("Ảnh không được vượt quá 10 MB.");
        }

        String accountId = required(r2Config.getAccountId(), "R2_ACCOUNT_ID");
        String accessKeyId = required(r2Config.getAccessKeyId(), "R2_ACCESS_KEY_ID");
        String secretAccessKey = required(r2Config.getSecretAccessKey(), "R2_SECRET_ACCESS_KEY");
        String bucket = required(r2Config.getBucket(), "R2_BUCKET");
        String publicBaseUrl = required(r2Config.getPublicBaseUrl(), "R2_PUBLIC_BASE_URL");

        if (!accountId.matches("[a-fA-F0-9]{32}")) {
            throw new IllegalStateException("R2_ACCOUNT_ID must be a 32-character Cloudflare account ID.");
        }
        URI publicUri = URI.create(publicBaseUrl);
        if (!"https".equalsIgnoreCase(publicUri.getScheme()) || publicUri.getHost() == null) {
            throw new IllegalStateException("R2_PUBLIC_BASE_URL must be an HTTPS public bucket URL.");
        }

        String objectKey = "product-images/" + UUID.randomUUID() + "." + extension;
        URI endpoint = URI.create("https://" + accountId + ".r2.cloudflarestorage.com");
        try (S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(PRESIGNED_URL_TTL)
                    .putObjectRequest(putObjectRequest)
                    .build();

            String uploadUrl = presigner.presignPutObject(presignRequest).url().toExternalForm();
            String imageUrl = publicBaseUrl.replaceAll("/+$", "") + "/" + objectKey;
            return new UploadDtos.PresignResponse(uploadUrl, imageUrl, contentType);
        }
    }

    private String required(String value, String variableName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Cloudflare R2 upload is not configured: set " + variableName + ".");
        }
        return value.trim();
    }
}
