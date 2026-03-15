package com.storix.storix_api.domains.user.application.client;

import com.storix.storix_api.domains.user.dto.NaverProfileResponse;
import com.storix.storix_api.global.config.web.NaverOauthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "NaverInfoClient",
        url = "https://openapi.naver.com",
        configuration = NaverOauthConfig.class
)
public interface NaverInfoClient {
    // 사용자 정보 조회
    @GetMapping("/v1/nid/me")
    NaverProfileResponse getUserInfo(@RequestHeader("Authorization") String authorization);
}
