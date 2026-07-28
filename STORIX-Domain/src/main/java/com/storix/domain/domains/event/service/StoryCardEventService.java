package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.StoryCardContentAdaptor;
import com.storix.domain.domains.event.adaptor.StoryCardDrawAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.StoryCardDraw;
import com.storix.domain.domains.event.domain.StoryCardGenres;
import com.storix.domain.domains.event.dto.StoryCardResponse;
import com.storix.domain.domains.event.dto.StoryCardStatusResponse;
import com.storix.domain.domains.event.exception.StoryCardEventNotActiveException;
import com.storix.domain.domains.event.exception.StoryCardEventNotFoundException;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class StoryCardEventService {

    private final AppEventAdaptor appEventAdaptor;
    private final StoryCardDrawAdaptor storyCardDrawAdaptor;
    private final StoryCardContentAdaptor storyCardContentAdaptor;

    @Transactional(readOnly = true)
    public StoryCardStatusResponse getStatus(Long appEventId, Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(appEventId);
        LocalDate serviceDate = StoryCardDraw.serviceDateOf(now);

        StoryCardResponse card = storyCardDrawAdaptor.findTodayDraw(event.getId(), userId, serviceDate)
                .map(draw -> StoryCardResponse.of(draw, true))
                .orElse(null);

        return StoryCardStatusResponse.builder()
                .appEventId(event.getId())
                .eventStartDate(startDateOf(event))
                .eventEndDate(endDateOf(event))
                .serviceDate(serviceDate)
                .eventActive(isActiveOn(event, now))
                .drawnToday(card != null)
                .card(card)
                .build();
    }

    @Transactional
    public StoryCardResponse draw(Long appEventId, Long userId, LocalDateTime now) {
        AppEvent event = resolveEvent(appEventId);
        if (!isActiveOn(event, now)) {
            throw StoryCardEventNotActiveException.EXCEPTION;
        }
        LocalDate serviceDate = StoryCardDraw.serviceDateOf(now);

        Optional<StoryCardDraw> alreadyDrawn = storyCardDrawAdaptor.findTodayDraw(event.getId(), userId, serviceDate);
        if (alreadyDrawn.isPresent()) {
            return StoryCardResponse.of(alreadyDrawn.get(), true);
        }

        Genre genre = randomGenre();
        StoryCardDraw draw = storyCardDrawAdaptor.saveIfAbsent(StoryCardDraw.of(
                event.getId(),
                userId,
                serviceDate,
                genre,
                storyCardContentAdaptor.pickMessage(genre),
                storyCardContentAdaptor.pickImmersion(),
                storyCardContentAdaptor.pickLuckyWork(genre),
                now
        ));

        // 동시 요청이면 saveIfAbsent가 먼저 저장된 행을 돌려주므로, 저장된 결과 기준으로 카드를 만든다
        return StoryCardResponse.of(draw, false);
    }

    private static Genre randomGenre() {
        return StoryCardGenres.SUPPORTED.get(
                ThreadLocalRandom.current().nextInt(StoryCardGenres.SUPPORTED.size()));
    }

    private AppEvent resolveEvent(Long appEventId) {
        if (appEventId == null || appEventId <= 0) {
            throw StoryCardEventNotFoundException.EXCEPTION;
        }
        return appEventAdaptor.findOptionalById(appEventId)
                .orElseThrow(() -> StoryCardEventNotFoundException.EXCEPTION);
    }

    private boolean isActiveOn(AppEvent event, LocalDateTime now) {
        return !now.isBefore(event.getStartAt()) && now.isBefore(event.getEndAt());
    }

    private static LocalDate startDateOf(AppEvent event) {
        return event.getStartAt().toLocalDate();
    }

    // end_at이 자정(다음 날 00:00, exclusive)으로 저장된 경우 마지막 참여 가능일은 그 전날
    private static LocalDate endDateOf(AppEvent event) {
        LocalDateTime endAt = event.getEndAt();
        return endAt.toLocalTime().equals(LocalTime.MIDNIGHT)
                ? endAt.toLocalDate().minusDays(1)
                : endAt.toLocalDate();
    }
}
