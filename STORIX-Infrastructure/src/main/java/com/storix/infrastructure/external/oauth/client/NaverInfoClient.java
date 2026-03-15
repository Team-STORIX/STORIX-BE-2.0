package com.storix.infrastructure.external.oauth.client;

import com.storix.domain.domains.user.dto.NaverProfileResponse;
import com.storix.infrastructure.external.oauth.config.NaverInfoConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "NaverInfoClient",
        url = "https://openapi.naver.com",
        configuration = NaverInfoConfig.class
)
public interface NaverInfoClient {
    // 사용자 정보 조회
    @GetMapping("/v1/nid/me")
    NaverProfileResponse getUserInfo(@RequestHeader("Authorization") String authorization);
}
