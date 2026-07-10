package com.storix.api.domain.user.controller.dto;

import com.storix.domain.domains.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminProfileResponse(
        @Schema(description = "관리자 닉네임")
        String nickName,

        @Schema(description = "관리자 이메일")
        String email
) {
    public static AdminProfileResponse from(User user) {
        return new AdminProfileResponse(user.getDisplayNickName(), user.getOauthInfo().getEmail());
    }
}
