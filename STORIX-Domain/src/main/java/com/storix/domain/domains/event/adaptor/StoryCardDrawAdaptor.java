package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.StoryCardDraw;
import com.storix.domain.domains.event.repository.StoryCardDrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    public boolean hasDrawn(Long appEventId, Long userId, LocalDate serviceDate) {
        return storyCardDrawRepository.existsByAppEventIdAndUserIdAndDrawnOn(appEventId, userId, serviceDate);
    }

    public StoryCardDraw saveIfAbsent(StoryCardDraw draw) {
        try {
            return storyCardDrawRepository.saveAndFlush(draw);
        } catch (DataIntegrityViolationException e) {
            return storyCardDrawRepository
                    .findByAppEventIdAndUserIdAndDrawnOn(draw.getAppEventId(), draw.getUserId(), draw.getDrawnOn())
                    .orElseThrow(() -> e);
        }
    }

    public int deleteDrawnBefore(LocalDate cutoff) {
        return storyCardDrawRepository.deleteAllDrawnBefore(cutoff);
    }
}
