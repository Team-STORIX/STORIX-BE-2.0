package com.storix.api.domain.aichat.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatWorksJson {

    private static final TypeReference<List<AiChatRawWork>> TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public String write(List<AiChatRawWork> works) {
        if (works.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(works);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("추천 작품 직렬화 실패", e);
        }
    }

    // 읽지 못한 기록은 카드 없이 보여 준다
    public List<AiChatRawWork> read(Long messageId, String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, TYPE);
        } catch (JsonProcessingException e) {
            log.warn(">>> [AiChat] works_json 읽기 실패 messageId={}", messageId);
            return List.of();
        }
    }

    public Map<Long, List<AiChatRawWork>> readAll(Collection<AiChatMessageInfo> messages) {
        Map<Long, List<AiChatRawWork>> result = new LinkedHashMap<>();
        messages.forEach(message -> result.put(message.messageId(), read(message.messageId(), message.worksJson())));
        return result;
    }

    public static Set<Long> workIds(Collection<List<AiChatRawWork>> works) {
        return works.stream()
                .flatMap(List::stream)
                .map(AiChatRawWork::workId)
                .collect(Collectors.toSet());
    }
}
