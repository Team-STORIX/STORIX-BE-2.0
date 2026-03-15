package com.storix.storix_api.domains.user.application.client;

import com.storix.storix_api.domains.user.dto.KakaoUserResponse;
import com.storix.storix_api.global.config.web.KakaoInfoConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "KakaoInfoClient",
        url = "https://kapi.kakao.com",
        configuration = KakaoInfoConfig.class
)
public interface KakaoInfoClient {
    // 사용자 정보 조회
    @GetMapping("/v2/user/me")
    KakaoUserResponse getUserInfo(@RequestHeader("Authorization") String authorization);

    // 사용자 연결 해제
    @PostMapping(value = "/v1/user/unlink", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void unlinkUser(
            @RequestHeader("Authorization") String adminKey,
            @RequestParam("target_id_type") String targetIdType,
            @RequestParam("target_id") Long aud
    );
}
