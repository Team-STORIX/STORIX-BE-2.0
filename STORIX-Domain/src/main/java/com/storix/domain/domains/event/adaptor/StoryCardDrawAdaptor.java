package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.StoryCardDraw;
import com.storix.domain.domains.event.dto.StoryCardDrawResult;
import com.storix.domain.domains.event.exception.StoryCardDrawFailedException;
import com.storix.domain.domains.event.repository.StoryCardDrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StoryCardDrawAdaptor {

    private final StoryCardDrawRepository storyCardDrawRepository;

    public Optional<StoryCardDraw> findTodayDraw(Long appEventId, Long userId, LocalDate serviceDate) {
        return storyCardDrawRepository.findByAppEventIdAndUserIdAndDrawnOn(appEventId, userId, serviceDate);
    }

    /**
     * 하루 한 장 제약을 지키며 저장한다. 중복이면 기존 행을 그대로 두는 no-op upsert라 예외가 나지 않는다.
     */
    public StoryCardDrawResult saveIfAbsent(StoryCardDraw candidate) {
        storyCardDrawRepository.insertIfAbsent(
                candidate.getAppEventId(),
                candidate.getUserId(),
                candidate.getDrawnOn(),
                candidate.getGenre().getDbValue(),
                candidate.getMessage(),
                candidate.getImmersion(),
                candidate.getLuckyWorkTitle(),
                candidate.getLuckyWorkType().getDbValue(),
                candidate.getLuckyWorkPlatform().name(),
                candidate.getLuckyWorkLandingUrl(),
                candidate.getDrawnAt()
        );
        StoryCardDraw saved = storyCardDrawRepository
                .findByAppEventIdAndUserIdAndDrawnOn(
                        candidate.getAppEventId(), candidate.getUserId(), candidate.getDrawnOn())
                .orElseThrow(() -> StoryCardDrawFailedException.EXCEPTION);

        return new StoryCardDrawResult(saved, saved.isSameCardAs(candidate));
    }

    public int deleteDrawnBefore(LocalDate cutoff) {
        return storyCardDrawRepository.deleteAllDrawnBefore(cutoff);
    }
}
