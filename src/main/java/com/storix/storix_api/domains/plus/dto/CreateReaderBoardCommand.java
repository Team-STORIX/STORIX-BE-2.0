package com.storix.storix_api.domains.plus.dto;

import com.storix.storix_api.domains.plus.domain.ReaderBoard;

import java.util.List;

public record CreateReaderBoardCommand(
        Long userId,
        boolean isWorksSelected,
        Long worksId,
        boolean isSpoiler,
        String content,
        List<String> objectKeys
) {
    public ReaderBoard toEntity() {
        return new ReaderBoard(
                userId,
                isWorksSelected,
                worksId,
                isSpoiler,
                content
        );
    }
}
