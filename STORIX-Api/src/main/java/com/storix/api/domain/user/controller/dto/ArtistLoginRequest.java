package com.storix.api.domain.user.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record ArtistLoginRequest(
        @NotBlank(message = "아이디 입력이 필수입니다.")
        String loginId,
        @NotBlank(message = "비밀번호 입력이 필수입니다.")
        String password
) {
}
