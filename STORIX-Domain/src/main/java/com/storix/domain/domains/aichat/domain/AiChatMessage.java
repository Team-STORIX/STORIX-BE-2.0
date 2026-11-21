package com.storix.domain.domains.aichat.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "ai_chat_messages",
        indexes = {
                @Index(name = "idx_ai_chat_message_chat", columnList = "ai_chat_id, ai_chat_message_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_chat_message_id")
    private Long id;

    @Column(name = "ai_chat_id", nullable = false)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiChatMessageRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiChatMessageType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 챗봇 답변에만 채운다
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AiChatAnswerStatus status;

    // 추천 작품 순위 · ID · 이유 · 일치 단서. 제목 · 표지 같은 카드 정보는 조회할 때 작품 DB 에서 채운다
    @Column(name = "works_json", columnDefinition = "TEXT")
    private String worksJson;

    @Column(name = "pipeline_version", length = 20)
    private String pipelineVersion;

    private AiChatMessage(Long chatId, AiChatMessageRole role, AiChatMessageType type, String content,
                          AiChatAnswerStatus status, String worksJson, String pipelineVersion) {
        this.chatId = chatId;
        this.role = role;
        this.type = type;
        this.content = content;
        this.status = status;
        this.worksJson = worksJson;
        this.pipelineVersion = pipelineVersion;
    }

    public static AiChatMessage question(Long chatId, String question) {
        return new AiChatMessage(chatId, AiChatMessageRole.USER, AiChatMessageType.TEXT, question, null, null, null);
    }

    public static AiChatMessage answer(Long chatId, String content, AiChatAnswerStatus status,
                                       String worksJson, String pipelineVersion) {
        return new AiChatMessage(chatId, AiChatMessageRole.CHATBOT, AiChatMessageType.TEXT, content,
                status, worksJson, pipelineVersion);
    }

    public static AiChatMessage onboarding(Long chatId, AiChatMessageRole role, String content) {
        return new AiChatMessage(chatId, role, AiChatMessageType.ONBOARDING, content, null, null, null);
    }
}
