package com.storix.domain.domains.review.dto;

import com.storix.domain.domains.plus.dto.ReviewInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record StandardReviewInfo(
        Long reviewId,
        boolean isSpoiler,
        String rating,
        String content,
        int likeCount,
        boolean isLiked,
        String lastCreatedTime
) {
    public static StandardReviewInfo fromReviewInfo(ReviewInfo reviewInfo, boolean isLiked) {
        return new StandardReviewInfo(
                reviewInfo.reviewId(),
                reviewInfo.isSpoiler(),
                reviewInfo.rating().getDbValue(),
                reviewInfo.content(),
                reviewInfo.likeCount(),
                isLiked,
                formatDate(reviewInfo.createdAt())
        );
    }

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd (EEE)", Locale.KOREAN);

    private static String formatDate(LocalDateTime time) {
        return time.format(DATE_FORMATTER);
    }
}
