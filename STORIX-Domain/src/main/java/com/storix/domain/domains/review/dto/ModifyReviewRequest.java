package com.storix.domain.domains.review.dto;

import com.storix.domain.domains.plus.domain.Rating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModifyReviewRequest(

        @NotNull(message = "별점을 선택해주세요.")
        Rating rating,

        @NotNull(message = "스포일러 여부를 선택해주세요.")
        boolean isSpoiler,

        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        @Size(max = 500, message = "리뷰는 500자까지 가능합니다.")
        String content

) {
}
