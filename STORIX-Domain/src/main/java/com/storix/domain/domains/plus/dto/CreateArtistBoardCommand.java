package com.storix.domain.domains.plus.dto;

import com.storix.domain.domains.plus.domain.ArtistBoard;

import java.util.List;

public record CreateArtistBoardCommand(
        Long userId,
        boolean isWorksSelected,
        Long worksId,
        boolean isContentForFan,
        Integer point,
        String content,
        List<String> objectKeys
) {
    public ArtistBoard toEntity() {
        return new ArtistBoard(
                userId,
                isWorksSelected,
                worksId,
                isContentForFan,
                point,
                content
        );
    }
}
