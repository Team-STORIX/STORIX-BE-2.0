package com.storix.api.domain.event.usecase;

import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.StoryCardResponse;
import com.storix.domain.domains.event.dto.StoryCardStatusResponse;
import com.storix.domain.domains.event.service.StoryCardEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class StoryCardEventUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final StoryCardEventService storyCardEventService;

    // 현재 진행 중인 오늘의 스토리 카드 이벤트의 app_event_id (미설정 시 0 → 404)
    @Value("${story-card-event.app-event-id:0}") private Long storyCardAppEventId;

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    // 오늘의 스토리 카드 현황 조회
    public CustomResponse<StoryCardStatusResponse> getStatus(Long userId) {

        return CustomResponse.onSuccess(
                SuccessCode.STORY_CARD_LOAD_SUCCESS,
                storyCardEventService.getStatus(storyCardAppEventId, userId, LocalDateTime.now(KST))
                        .withBaseUrl(baseUrl)
        );
    }

    // 카드 뽑기
    public CustomResponse<StoryCardResponse> draw(Long userId) {

        return CustomResponse.onSuccess(
                SuccessCode.STORY_CARD_DRAW_SUCCESS,
                storyCardEventService.draw(storyCardAppEventId, userId, LocalDateTime.now(KST))
                        .withBaseUrl(baseUrl)
        );
    }
}
