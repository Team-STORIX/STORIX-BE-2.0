package com.storix.storix_api.domains.user.dto;

public record OIDCDecodePayload(
        String iss, // ID 토큰 발급 인증 기관 정보
        String aud, // ID 토큰이 발급된 앱의 앱 키
        String sub  // ID 토큰에 해당하는 사용자의 회원번호
) {
}
