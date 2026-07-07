package com.storix.domain.domains.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.domain.domains.event.domain.UserAppEvent;
import com.storix.domain.domains.event.domain.UserAppEventStatus;
import com.storix.domain.domains.event.domain.UserAppEventType;
import com.storix.domain.domains.event.adaptor.UserAppEventAdaptor;
import com.storix.domain.domains.event.dto.OneTimeAppEventResponse;
import com.storix.domain.domains.event.exception.UserAppEventPayloadSerializationException;
import com.storix.domain.domains.user.domain.Title;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAppEventService {

    private static final int PENDING_EVENT_LIMIT = 10;

    private final UserAppEventAdaptor userAppEventAdaptor;
    private final UserAppEventCacheHelper userAppEventCacheHelper;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserAppEvent createTitleAcquiredEvent(Long userId, Title title, LocalDateTime acquiredAt) {
        String payloadJson = toJson(Map.of(
                "title", title.getDisplayName(),
                "genre", title.getGenre().name()
        ));

        UserAppEvent saved = userAppEventAdaptor.save(UserAppEvent.builder()
                .userId(userId)
                .eventType(UserAppEventType.TITLE_ACQUIRED)
                .payloadJson(payloadJson)
                .occurredAt(acquiredAt)
                .build());
        userAppEventCacheHelper.evict(userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<OneTimeAppEventResponse> getPendingEvents(Long userId) {
        return userAppEventCacheHelper.getPendingEvents(userId, () -> loadPendingEvents(userId));
    }

    private List<OneTimeAppEventResponse> loadPendingEvents(Long userId) {
        return userAppEventAdaptor.findPendingByUserId(userId, PageRequest.of(0, PENDING_EVENT_LIMIT))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void ack(Long userId, Long eventId) {
        UserAppEvent event = userAppEventAdaptor.findByIdAndOwner(eventId, userId);
        if (event.getStatus() == UserAppEventStatus.PENDING) {
            event.ack();
            userAppEventCacheHelper.evict(userId);
        }
    }

    private OneTimeAppEventResponse toResponse(UserAppEvent event) {
        return new OneTimeAppEventResponse(
                event.getId(),
                event.getEventType().name(),
                true,
                parsePayload(event.getPayloadJson())
        );
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw UserAppEventPayloadSerializationException.EXCEPTION;
        }
    }

    private Map<String, Object> parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(payloadJson, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
