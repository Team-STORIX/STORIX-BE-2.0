package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.StoryCardContentAdaptor;
import com.storix.domain.domains.event.adaptor.StoryCardDrawAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.domain.StoryCardDraw;
import com.storix.domain.domains.event.domain.StoryCardGenres;
import com.storix.domain.domains.event.dto.StoryCardDrawResult;
import com.storix.domain.domains.event.dto.StoryCardLuckyWorkPick;
import com.storix.domain.domains.event.dto.StoryCardResponse;
import com.storix.domain.domains.event.dto.StoryCardStatusResponse;
import com.storix.domain.domains.event.exception.StoryCardContentNotFoundException;
import com.storix.domain.domains.event.exception.StoryCardEventNotActiveException;
import com.storix.domain.domains.event.exception.StoryCardEventNotFoundException;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class StoryCardEventService {

    private final AppEventAdaptor appEventAdaptor;
    private final StoryCardDrawAdaptor storyCardDrawAdaptor;
    private final StoryCardContentAdaptor storyCardContentAdaptor;
    private final WorksAdaptor worksAdaptor;

    @Transactional(readOnly = true)
    public StoryCardStatusResponse getStatus(Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(now);
        LocalDate serviceDate = event.serviceDateOf(now);

        StoryCardResponse card = storyCardDrawAdaptor.findTodayDraw(event.getId(), userId, serviceDate)
                .map(draw -> StoryCardResponse.of(draw, true))
                .orElse(null);

        return StoryCardStatusResponse.builder()
                .appEventId(event.getId())
                .eventStartDate(event.participationStartDate())
                .eventEndDate(event.participationEndDate())
                .serviceDate(serviceDate)
                .eventActive(event.isActiveAt(now))
                .drawnToday(card != null)
                .card(card)
                .build();
    }

    @Transactional
    public StoryCardResponse draw(Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(now);
        if (!event.isActiveAt(now)) {
            throw StoryCardEventNotActiveException.EXCEPTION;
        }
        LocalDate serviceDate = event.serviceDateOf(now);

        Optional<StoryCardDraw> alreadyDrawn = storyCardDrawAdaptor.findTodayDraw(event.getId(), userId, serviceDate);
        if (alreadyDrawn.isPresent()) {
            return StoryCardResponse.of(alreadyDrawn.get(), true);
        }

        Genre genre = randomGenre();
        StoryCardDrawResult result = storyCardDrawAdaptor.saveIfAbsent(StoryCardDraw.of(
                event.getId(),
                userId,
                serviceDate,
                genre,
                storyCardContentAdaptor.pickMessage(genre),
                storyCardContentAdaptor.pickImmersion(),
                pickLuckyWork(genre),
                now
        ));

        // 사전 조회를 동시에 통과한 경우 저장에 실패한 쪽은 먼저 저장된 카드를 그대로 받는다
        return StoryCardResponse.of(result.draw(), !result.created());
    }

    private static Genre randomGenre() {
        return StoryCardGenres.SUPPORTED.get(
                ThreadLocalRandom.current().nextInt(StoryCardGenres.SUPPORTED.size()));
    }

    // 행운의 작품은 works에서 관리한다 (is_story_card_lucky_work)
    private StoryCardLuckyWorkPick pickLuckyWork(Genre genre) {
        return worksAdaptor.pickStoryCardLuckyWork(genre)
                .orElseThrow(() -> StoryCardContentNotFoundException.EXCEPTION);
    }

    // 진행 중인 STORY_CARD 이벤트를 검색. 없으면 종료 → 예정 순으로 폴백
    private AppEvent resolveEvent(LocalDateTime now) {
        return appEventAdaptor.findActiveOrNearestByType(AppEventType.STORY_CARD, now)
                .orElseThrow(() -> StoryCardEventNotFoundException.EXCEPTION);
    }
}
