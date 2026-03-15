package com.storix.storix_api.domains.plus.controller.dto;

import com.storix.storix_api.domains.plus.domain.Rating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReaderReviewUploadRequest(

        Long worksId,

        @NotNull(message = "별점을 선택해주세요.")
        Rating rating,

        boolean isSpoiler,

        @Size(max = 500, message = "리뷰는 500자까지 가능합니다.")
        String content

) {
}