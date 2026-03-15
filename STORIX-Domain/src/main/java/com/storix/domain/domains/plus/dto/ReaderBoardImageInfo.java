package com.storix.domain.domains.plus.dto;

import com.storix.domain.domains.plus.domain.ReaderBoardImage;

public record ReaderBoardImageInfo(
        Long boardId,
        String imageUrl,
        int sortOrder
) {
    public static ReaderBoardImageInfo from(
            ReaderBoardImage entity,
            String baseUrl
    ) {
        String url = entity.getImageObjectKey();
        if (url != null && !url.isBlank()) {
            url = baseUrl + "/" + url;
        }

        return new ReaderBoardImageInfo(
                entity.getReaderBoard().getId(),
                url,
                entity.getSortOrder()
        );
    }
}