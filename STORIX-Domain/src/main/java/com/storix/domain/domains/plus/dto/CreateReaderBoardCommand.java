package com.storix.domain.domains.plus.dto;

import com.storix.domain.domains.feed.domain.BoardTheme;
import com.storix.domain.domains.plus.domain.ReaderBoard;

import java.util.List;

public record CreateReaderBoardCommand(
        Long userId,
        boolean isWorksSelected,
        Long worksId,
        boolean isSpoiler,
        String spoilerScript,
        String content,
        BoardTheme theme,
        List<String> objectKeys
) {
    public ReaderBoard toEntity() {
        return new ReaderBoard(
                userId,
                isWorksSelected,
                worksId,
                isSpoiler,
                spoilerScript,
                content,
                theme
        );
    }
}
