package com.storix.api.domain.adultverification.helper;

import com.storix.common.property.PortOneProperties;
import com.storix.domain.domains.adultverification.dto.IdentityVerificationResult;
import com.storix.infrastructure.external.portone.client.PortOneIdentityVerificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityVerificationHelper {

    private static final String AUTHORIZATION_PREFIX = "PortOne ";

    private final PortOneIdentityVerificationClient portOneIdentityVerificationClient;
    private final PortOneProperties portOneProperties;

    public IdentityVerificationResult getVerification(String identityVerificationId) {
        return portOneIdentityVerificationClient
                .getIdentityVerification(
                        AUTHORIZATION_PREFIX + portOneProperties.getApiSecret(),
                        identityVerificationId
                )
                .toResult();
    }
}
