package com.storix.domain.domains.user.dto;

import jakarta.validation.constraints.*;

public record AdminSignupRequest(

    @NotBlank(message = "관리자 식별자는 필수입니다.")
    String adminIdentifier,

    @NotBlank(message = "아이디는 필수입니다.")
    @Email(message = "올바른 email 형식이 아닙니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8~30자까지 가능합니다.")
    String password,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자까지 가능합니다.")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ ]+$",
            message = "닉네임은 한글, 영문, 숫자, 공백만 가능합니다."
    )
    String nickName
) {
}
