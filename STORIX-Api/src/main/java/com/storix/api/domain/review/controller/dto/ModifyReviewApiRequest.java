package com.storix.api.domain.review.controller.dto;

import com.storix.api.global.validation.RequiredIf;
import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.review.dto.ModifyReviewRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RequiredIf(when = "isSpoiler", is = "true", field = "spoilerScript",
        message = "스포일러 게시물은 가림막 문구가 필요합니다.")
public record ModifyReviewApiRequest(

        @Schema(description = "별점")
        @NotNull(message = "별점을 선택해주세요.")
        Rating rating,

        @Schema(description = "스포일러 여부")
        @NotNull(message = "스포일러 여부를 선택해주세요.")
        Boolean isSpoiler,

        @Schema(description = "스포일러 가림막 문구. isSpoiler 가 true 면 필수")
        String spoilerScript,

        @Schema(description = "리뷰 내용")
        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        @Size(max = 500, message = "리뷰는 500자까지 가능합니다.")
        String content

) {

    public ModifyReviewRequest toDto() {
        return new ModifyReviewRequest(rating, Boolean.TRUE.equals(isSpoiler), spoilerScript, content);
    }
}
