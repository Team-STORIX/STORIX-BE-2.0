package com.storix.api.domain.aichat.stream;

import com.storix.api.domain.aichat.controller.dto.AiChatMatchedClue;

import java.util.List;

// AI 가 보내는 추천 작품. works_json 에 이 모양 그대로 저장하고, 카드 정보는 보여 줄 때 작품 DB 에서 채운다
public record AiChatRawWork(int rank, Long workId, String reason, List<AiChatMatchedClue> matchedClues) {
}
