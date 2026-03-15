package com.storix.storix_api.domains.user.controller.dto;

import com.storix.storix_api.domains.user.domain.Gender;
import com.storix.storix_api.domains.works.domain.Genre;
import jakarta.validation.constraints.*;

import java.util.Set;

public record ReaderSignupRequest(

    @AssertTrue(message = "마케팅 동의는 필수입니다.")
    Boolean marketingAgree,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자까지 가능합니다.")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ ]+$",
            message = "닉네임은 한글, 영문, 숫자, 공백만 가능하며 자음/모음/공백만으로는 불가능합니다."
    )
    String nickName,

    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @NotNull(message = "관심 장르는 필수입니다.")
    @Size(min = 1, max = 3, message = "관심 장르는 1개 이상 3개 이하로 선택해야 합니다.")
    Set<Genre> favoriteGenreList,

    @NotNull(message = "관심 작품은 필수입니다.")
    @Size(min = 2, max = 18, message = "관심 작품은 2개 이상 18개 이하로 선택해야 합니다.")
    Set<Long> favoriteWorksIdList
) {
}
