package com.storix.storix_api.domains.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OIDCPublicKeysResponse {
    private List<OIDCPublicKeyDTO> keys;
}