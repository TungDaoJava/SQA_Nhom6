package com.routine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class UploadDtos {

    public record PresignRequest(@NotBlank String contentType, @Positive long size) {
    }

    public record PresignResponse(String uploadUrl, String imageUrl, String contentType) {
    }
}
