package com.storix.domain.domains.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
    @NotBlank(message = "아이디는 필수입니다.")
    @Email(message = "올바른 email 형식이 아닙니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {
}
