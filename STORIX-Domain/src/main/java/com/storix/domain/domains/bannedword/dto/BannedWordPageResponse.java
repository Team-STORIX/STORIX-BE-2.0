package com.storix.domain.domains.bannedword.dto;

import com.storix.domain.domains.bannedword.domain.BannedWord;
import org.springframework.data.domain.Page;

import java.util.List;

public record BannedWordPageResponse(
        List<BannedWordResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static BannedWordPageResponse from(Page<BannedWord> page) {
        return new BannedWordPageResponse(
                page.getContent().stream().map(BannedWordResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
