package com.storix.infrastructure.external.oauth.client;

import com.storix.domain.domains.user.dto.XProfileResponse;
import com.storix.infrastructure.external.oauth.config.XInfoConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "XInfoClient",
        url = "https://api.x.com",
        configuration = XInfoConfig.class
)
public interface XInfoClient {
    // 사용자 정보 조회 (accessToken)
    @GetMapping("/2/users/me")
    XProfileResponse getUserInfo(@RequestHeader("Authorization") String authorization);
}
