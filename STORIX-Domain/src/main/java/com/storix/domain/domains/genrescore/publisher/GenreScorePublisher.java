package com.storix.domain.domains.genrescore.publisher;

import com.storix.domain.domains.genrescore.event.GenreScoreEvent;
import com.storix.domain.domains.genrescore.event.GenreScoreEventType;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreScorePublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final LoadWorksPort loadWorksPort;

    // 작품 id값만 알고 있는 발행 지점 publish
    public void publish(Long userId, Long worksId, GenreScoreEventType type) {
        try {
            Genre genre = loadWorksPort.findById(worksId).getGenre();
            if (genre == null) {
                log.warn(">>> [GenreScore] genre missing for worksId={}, type={}", worksId, type);
                return;
            }
            eventPublisher.publishEvent(GenreScoreEvent.of(userId, worksId, genre, type));
        } catch (Exception e) {
            log.warn(">>> [GenreScore] publish failed userId={}, worksId={}, type={}, cause={}",
                    userId, worksId, type, e.getMessage());
        }
    }

    // genre 값만 알고 있는 발행 지점 publish
    public void publishWithGenre(Long userId, Long worksId, Genre genre, GenreScoreEventType type) {
        if (genre == null) {
            log.warn(">>> [GenreScore] genre missing publishWithGenre worksId={}, type={}", worksId, type);
            return;
        }
        try {
            eventPublisher.publishEvent(GenreScoreEvent.of(userId, worksId, genre, type));
        } catch (Exception e) {
            log.warn(">>> [GenreScore] publish failed userId={}, worksId={}, type={}, cause={}",
                    userId, worksId, type, e.getMessage());
        }
    }

    // 여러 작품 발행 지점 publish
    public void publishBatch(Long userId, Collection<Long> worksIds, GenreScoreEventType type) {
        if (worksIds == null || worksIds.isEmpty()) return;
        try {
            for (Works works : loadWorksPort.findWorksByIds(worksIds.stream().toList())) {
                if (works.getGenre() == null) continue;
                eventPublisher.publishEvent(
                        GenreScoreEvent.of(userId, works.getId(), works.getGenre(), type));
            }
        } catch (Exception e) {
            log.warn(">>> [GenreScore] batch publish failed userId={}, type={}, cause={}",
                    userId, type, e.getMessage());
        }
    }
}
