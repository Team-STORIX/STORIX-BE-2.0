package com.storix.domain.domains.image.dto;

public record PresignedUrlResponse(
        String url,
        String objectKey,
        Long expiresInSeconds
) {}