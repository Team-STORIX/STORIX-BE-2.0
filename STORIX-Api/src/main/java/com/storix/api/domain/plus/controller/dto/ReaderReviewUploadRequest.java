package com.storix.api.domain.plus.controller.dto;

import com.storix.api.global.validation.RequiredIf;
import com.storix.domain.domains.plus.domain.Rating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RequiredIf(when = "isSpoiler", is = "true", field = "spoilerScript",
        message = "스포일러 게시물은 가림막 문구가 필요합니다.")
public record ReaderReviewUploadRequest(

        Long worksId,

        @NotNull(message = "별점을 선택해주세요.")
        Rating rating,

        @NotNull(message = "스포일러 여부를 선택해주세요.")
        Boolean isSpoiler,

        String spoilerScript,

        @Size(max = 500, message = "리뷰는 500자까지 가능합니다.")
        String content

) {
}