package com.storix.api.domain.aichat.stream.dto;

import java.util.Set;

public record AiServerErrorEvent(
        String code,
        String message
) {
    public static final String TIMEOUT = "TIMEOUT";
    public static final String INTERNAL = "INTERNAL";

    private static final Set<String> APP_CODES = Set.of(TIMEOUT, "LLM_UNAVAILABLE", "SEARCH_UNAVAILABLE", INTERNAL);
    private static final String TIMEOUT_MESSAGE = "답변이 늦어지고 있어요. 잠시 후 다시 시도해 주세요.";
    private static final String ERROR_MESSAGE = "일시적인 오류로 검색하지 못했어요. 잠시 후 다시 시도해 주세요.";

    // 앱에 넘길 코드. INVALID_REQUEST 등은 Spring 쪽 문제라 INTERNAL
    public String appCode() {
        return APP_CODES.contains(code) ? code : INTERNAL;
    }

    public static String appMessage(String appCode) {
        return TIMEOUT.equals(appCode) ? TIMEOUT_MESSAGE : ERROR_MESSAGE;
    }
}
