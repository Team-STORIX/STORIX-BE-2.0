package com.storix.api.domain.aichat.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.api.domain.aichat.controller.dto.AiChatRawWork;
import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXDynamicException;
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
public class AiChatWorksJsonHelper {

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
            throw new STORIXDynamicException(ErrorCode.INTERNAL_SERVER_ERROR, "추천 작품 직렬화 실패", e);
        }
    }

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

    public Map<Long, List<Long>> readWorkIds(Collection<AiChatMessageInfo> messages) {
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        readAll(messages).forEach((messageId, works) ->
                result.put(messageId, works.stream().map(AiChatRawWork::workId).toList()));
        return result;
    }

    public static Set<Long> workIds(Collection<List<AiChatRawWork>> works) {
        return works.stream()
                .flatMap(List::stream)
                .map(AiChatRawWork::workId)
                .collect(Collectors.toSet());
    }
}
