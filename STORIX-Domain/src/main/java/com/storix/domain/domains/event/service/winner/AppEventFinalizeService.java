package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.dto.AppEventDrawWinner;
import com.storix.domain.domains.event.dto.AppEventWinnerDrawResponse;
import com.storix.domain.domains.event.dto.EventWinner;
import com.storix.domain.domains.event.exception.AppEventInvalidWinnerCountException;
import com.storix.domain.domains.event.exception.AppEventNoWinnerException;
import com.storix.domain.domains.event.exception.AppEventNotEndedException;
import com.storix.domain.domains.event.exception.EventWinnerFinalizerNotImplementedException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppEventFinalizeService {

    // 구현체 없으면 Spring이 빈 리스트 주입
    private final List<EventWinnerFinalizer> finalizers;
    private final AppEventWinnerService appEventWinnerService;
    private final AppEventAdaptor appEventAdaptor;
    private final UserAdaptor userAdaptor;

    // 당첨자 확정 및 저장, 이미 확정된 이벤트면 재추첨하지 않고 기존 당첨자 반환
    // 이벤트 유형별 분기는 finalizer.supports() 가 전담하므로 유형을 여기서 좁히지 않는다
    @Transactional
    public AppEventWinnerDrawResponse finalizeWinners(Long appEventId, int winnerCount) {
        if (winnerCount <= 0) {
            throw AppEventInvalidWinnerCountException.EXCEPTION;
        }
        // 동시 호출로 서로 다른 추첨 결과가 섞이지 않도록 이벤트 행을 잠그고 진행한다
        AppEvent event = appEventAdaptor.findByIdForUpdate(appEventId);
        if (!event.isHasWinner()) {
            throw AppEventNoWinnerException.EXCEPTION;
        }
        // 확정된 결과는 다시 뽑지 않는다
        List<EventWinner> confirmed = appEventWinnerService.findWinners(appEventId);
        if (!confirmed.isEmpty()) {
            log.atInfo()
                    .addKeyValue("appEventId", appEventId)
                    .addKeyValue("appEventType", event.getEventType())
                    .addKeyValue("winnerCount", confirmed.size())
                    .log(">>> [AppEvent] 이미 확정된 이벤트 - 재추첨 없이 반환");
            return toResponse(appEventId, true, confirmed);
        }

        // 추첨은 되돌릴 수 없어 진행 중 확정 시 남은 기간 참여자가 영구 제외된다.
        // 조기 확정이 필요하면 이벤트를 강제 종료해 end_at을 당긴 뒤 확정한다
        if (!event.isEndedAt(LocalDateTime.now())) {
            throw AppEventNotEndedException.EXCEPTION;
        }

        EventWinnerFinalizer finalizer = finalizers.stream()
                .filter(f -> f.supports(event))
                .findFirst()
                .orElseThrow(() -> EventWinnerFinalizerNotImplementedException.EXCEPTION);

        List<EventWinner> winners = finalizer.resolveWinners(event, winnerCount);
        appEventWinnerService.saveWinners(appEventId, winners);
        log.atInfo()
                .addKeyValue("appEventId", appEventId)
                .addKeyValue("appEventType", event.getEventType())
                .addKeyValue("requestedWinnerCount", winnerCount)
                .addKeyValue("winnerCount", winners.size())
                .log(">>> [AppEvent] 당첨자 확정 완료");
        return toResponse(appEventId, false, winners);
    }

    private AppEventWinnerDrawResponse toResponse(Long appEventId, boolean alreadyFinalized, List<EventWinner> winners) {
        Map<Long, String> nickNames = userAdaptor.findNicknameMapByUserIds(
                winners.stream().map(EventWinner::userId).toList()
        );

        return AppEventWinnerDrawResponse.builder()
                .appEventId(appEventId)
                .alreadyFinalized(alreadyFinalized)
                .winners(winners.stream()
                        .map(winner -> AppEventDrawWinner.builder()
                                .drawOrder(winner.drawOrder())
                                .userId(winner.userId())
                                .nickName(nickNames.get(winner.userId()))
                                .build())
                        .toList())
                .build();
    }
}
