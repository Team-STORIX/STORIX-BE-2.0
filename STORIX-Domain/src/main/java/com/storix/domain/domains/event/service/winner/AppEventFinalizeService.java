package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.exception.EventWinnerFinalizerNotImplementedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppEventFinalizeService {

    // 구현체 없으면 Spring이 빈 리스트 주입
    private final List<EventWinnerFinalizer> finalizers;
    private final AppEventParticipantService appEventParticipantService;

    @Transactional
    public void finalizeWinners(AppEvent event) {
        if (!event.isHasWinner()) return;

        EventWinnerFinalizer finalizer = finalizers.stream()
                .filter(f -> f.supports(event))
                .findFirst()
                .orElseThrow(() -> EventWinnerFinalizerNotImplementedException.EXCEPTION);

        appEventParticipantService.markWinners(event.getId(), finalizer.resolveWinnerUserIds(event));
    }
}
