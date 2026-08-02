package com.storix.domain.domains.user.dto;

// 관리자 화면용 유저 식별/연락 정보 (닉네임은 탈퇴 시 마스킹된 표시명)
public record AdminUserContactInfo(
        Long userId,
        String nickName,
        String email,
        String profileImageUrl
) {
    public AdminUserContactInfo withBaseUrl(String baseUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return this;
        }

        return new AdminUserContactInfo(
                userId,
                nickName,
                email,
                baseUrl + "/" + profileImageUrl
        );
    }
}
