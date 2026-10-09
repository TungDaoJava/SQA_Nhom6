package com.routine.controller;

import com.routine.dto.UploadDtos;
import com.routine.service.R2UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final R2UploadService r2UploadService;

    @PostMapping("/presign")
    @PreAuthorize("hasRole('ADMIN')")
    public UploadDtos.PresignResponse presignProductImage(
            @Valid @RequestBody UploadDtos.PresignRequest request) {
        return r2UploadService.createProductImageUpload(request);
    }
}
