package com.storix.domain.domains.bannedword.dto;

import com.storix.domain.domains.bannedword.domain.BannedWord;

import java.time.LocalDateTime;

public record BannedWordResponse(
        Long id,
        String word,
        LocalDateTime createdAt
) {

    public static BannedWordResponse from(BannedWord bannedWord) {
        return new BannedWordResponse(
                bannedWord.getId(),
                bannedWord.getWord(),
                bannedWord.getCreatedAt()
        );
    }
}
