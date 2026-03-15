package com.storix.storix_api.global.apiPayload;

import java.util.List;

public class STORIXStatic {
    public static final String BEARER = "Bearer ";
    public static final String TOKEN_TYPE = "type";
    public static final String TOKEN_ROLE = "role";
    public static final String TOKEN_ISSUR = "STORIX";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ONBOARDING_TOKEN = "onboarding_token";
    public static final String KID = "kid";
    public static final String WITHDRAW_PREFIX = "DELETED:";

    public static final int MILLI_TO_SECOND = 1000;

    public static final List<String> SWAGGER_URI= List.of(
            new String[]{"/swagger-resources/", "/swagger-ui/", "/v3/api-docs"}
    );
}
