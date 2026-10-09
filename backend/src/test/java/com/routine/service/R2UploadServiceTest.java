package com.routine.service;

import com.routine.config.R2Config;
import com.routine.dto.UploadDtos;
import com.routine.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class R2UploadServiceTest {

    private R2UploadService service;

    @BeforeEach
    void setUp() {
        R2Config config = new R2Config();
        config.setAccountId("0123456789abcdef0123456789abcdef");
        config.setAccessKeyId("test-access-key");
        config.setSecretAccessKey("test-secret-key");
        config.setBucket("routine-images");
        config.setPublicBaseUrl("https://images.example.com/");
        service = new R2UploadService(config);
    }

    @Test
    void createsShortLivedPutUrlAndPublicImageUrl() {
        UploadDtos.PresignResponse response = service.createProductImageUpload(
                new UploadDtos.PresignRequest("image/png", 1024));

        assertThat(response.uploadUrl())
                .startsWith("https://0123456789abcdef0123456789abcdef.r2.cloudflarestorage.com/")
                .contains("/routine-images/product-images/")
                .contains("X-Amz-Signature=");
        assertThat(response.imageUrl())
                .startsWith("https://images.example.com/product-images/")
                .endsWith(".png");
        assertThat(response.contentType()).isEqualTo("image/png");
    }

    @Test
    void rejectsUnsupportedImageTypes() {
        assertThatThrownBy(() -> service.createProductImageUpload(
                new UploadDtos.PresignRequest("text/html", 100)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsImagesLargerThanTenMegabytes() {
        assertThatThrownBy(() -> service.createProductImageUpload(
                new UploadDtos.PresignRequest("image/jpeg", 10L * 1024 * 1024 + 1)))
                .isInstanceOf(BadRequestException.class);
    }
}
