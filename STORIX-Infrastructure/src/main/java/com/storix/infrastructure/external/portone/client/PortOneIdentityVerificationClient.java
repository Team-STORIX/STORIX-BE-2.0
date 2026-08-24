package com.storix.infrastructure.external.portone.client;

import com.storix.infrastructure.external.portone.config.PortOneClientConfig;
import com.storix.infrastructure.external.portone.dto.PortOneIdentityVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "PortOneIdentityVerificationClient",
        url = "${portone.base-url}",
        configuration = PortOneClientConfig.class
)
public interface PortOneIdentityVerificationClient {

    // 본인인증 단건 조회
    @GetMapping("/identity-verifications/{identityVerificationId}")
    PortOneIdentityVerificationResponse getIdentityVerification(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("identityVerificationId") String identityVerificationId
    );
}
