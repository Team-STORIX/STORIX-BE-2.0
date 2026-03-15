package com.storix.storix_api.domains.image.dto;

public record PresignedUrlResponse(
        String url,
        String objectKey,
        Long expiresInSeconds
) {}