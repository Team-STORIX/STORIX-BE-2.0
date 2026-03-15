package com.storix.domain.domains.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자까지 가능합니다.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ ]+$",
                message = "닉네임은 한글, 영문, 숫자, 공백만 가능하며 자음/모음/공백만으로는 불가능합니다."
        )
        String nickName
) {
}
