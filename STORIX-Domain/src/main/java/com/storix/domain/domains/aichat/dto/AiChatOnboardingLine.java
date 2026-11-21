package com.storix.domain.domains.aichat.dto;

import com.storix.domain.domains.aichat.domain.AiChatMessageRole;

// 온보딩을 마칠 때 대화 기록에 남기는 말풍선 한 줄
public record AiChatOnboardingLine(AiChatMessageRole role, String content) {
}
