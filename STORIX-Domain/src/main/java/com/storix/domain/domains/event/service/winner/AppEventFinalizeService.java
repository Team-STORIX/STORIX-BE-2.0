package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.dto.EventWinner;
import com.storix.domain.domains.event.exception.AppEventNoWinnerException;
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
    private final AppEventWinnerService appEventWinnerService;
    private final AppEventAdaptor appEventAdaptor;

    // 당첨자 확정 및 저장, 이미 확정된 이벤트면 재추첨하지 않고 기존 당첨자 반환
    @Transactional
    public List<EventWinner> finalizeWinners(Long appEventId, int winnerCount) {
        // 동시 호출로 서로 다른 추첨 결과가 섞이지 않도록 이벤트 행을 잠그고 진행한다
        AppEvent event = appEventAdaptor.findByIdForUpdate(appEventId);
        if (!event.isHasWinner()) {
            throw AppEventNoWinnerException.EXCEPTION;
        }

        List<EventWinner> confirmed = appEventWinnerService.findWinners(appEventId);
        if (!confirmed.isEmpty()) {
            return confirmed;
        }

        EventWinnerFinalizer finalizer = finalizers.stream()
                .filter(f -> f.supports(event))
                .findFirst()
                .orElseThrow(() -> EventWinnerFinalizerNotImplementedException.EXCEPTION);

        List<EventWinner> winners = finalizer.resolveWinners(event, winnerCount);
        appEventWinnerService.saveWinners(appEventId, winners);
        return winners;
    }
}
