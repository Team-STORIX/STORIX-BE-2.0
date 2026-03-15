package com.storix.storix_api.domains.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OIDCPublicKeyDTO {
    private String kid;
    private String kty;
    private String alg; // 카카오만
    private String use; // 카카오만
    private String n;
    private String e;
}