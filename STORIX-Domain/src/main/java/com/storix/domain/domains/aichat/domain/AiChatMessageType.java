package com.storix.domain.domains.aichat.domain;

// ONBOARDING 은 화면 대화 기록에만 남기고 AI 서버에 넘기는 맥락에서는 뺀다
public enum AiChatMessageType {
    TEXT,
    ONBOARDING
}
