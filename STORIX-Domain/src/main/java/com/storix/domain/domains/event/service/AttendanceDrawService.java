package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawWinner;
import com.storix.domain.domains.event.dto.AttendanceTicketHolder;
import com.storix.domain.domains.event.dto.EventWinner;
import com.storix.domain.domains.event.exception.AttendanceDrawInvalidWinnerCountException;
import com.storix.domain.domains.event.exception.AttendanceEventNotFoundException;
import com.storix.domain.domains.event.service.winner.AppEventFinalizeService;
import com.storix.domain.domains.event.service.winner.AppEventWinnerService;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceDrawService {

    private final AppEventAdaptor appEventAdaptor;
    private final AttendanceCheckAdaptor attendanceCheckAdaptor;
    private final AppEventFinalizeService appEventFinalizeService;
    private final AppEventWinnerService appEventWinnerService;
    private final UserAdaptor userAdaptor;

    // 당첨자 확정
    @Transactional
    public AttendanceDrawResponse draw(Long appEventId, int winnerCount) {
        if (winnerCount <= 0) {
            throw AttendanceDrawInvalidWinnerCountException.EXCEPTION;
        }
        AppEvent event = resolveEvent(appEventId);

        return toResponse(event, appEventFinalizeService.finalizeWinners(appEventId, winnerCount));
    }

    // 당첨자 조회
    // 아직 추첨하지 않은 이벤트면 당첨자 목록은 비어 있고 모수 통계만 채워진다.
    @Transactional(readOnly = true)
    public AttendanceDrawResponse findWinners(Long appEventId) {
        AppEvent event = resolveEvent(appEventId);

        return toResponse(event, appEventWinnerService.findWinners(appEventId));
    }

    // 모수 통계와 당첨자별 응모권 수는 저장하지 않고 매번 계산, 당첨자는 변경되지 않음
    private AttendanceDrawResponse toResponse(AppEvent event, List<EventWinner> winners) {
        List<AttendanceTicketHolder> candidates = AttendanceTicketPolicy.ticketHoldersOf(
                event, attendanceCheckAdaptor.findAttendeeCounts(event.getId())
        );

        return AttendanceDrawResponse.builder()
                .appEventId(event.getId())
                .candidateCount(candidates.size())
                .totalTickets(candidates.stream().mapToInt(AttendanceTicketHolder::ticketCount).sum())
                .winners(toWinners(winners, candidates))
                .build();
    }

    // 관리자가 지정한 이벤트로 추첨한다. 종료된 이벤트도 대상이므로 진행 여부는 보지 않지만,
    // 출석 이벤트가 아니면 응모권 개념이 없으므로 404로 막는다
    private AppEvent resolveEvent(Long appEventId) {
        if (appEventId == null || appEventId <= 0) {
            throw AttendanceEventNotFoundException.EXCEPTION;
        }
        return appEventAdaptor.findOptionalById(appEventId)
                .filter(event -> event.getEventType() == AppEventType.ATTENDANCE)
                .orElseThrow(() -> AttendanceEventNotFoundException.EXCEPTION);
    }

    private List<AttendanceDrawWinner> toWinners(List<EventWinner> winners,
                                                 List<AttendanceTicketHolder> candidates) {
        Map<Long, AttendanceTicketHolder> holderByUserId = candidates.stream()
                .collect(Collectors.toMap(AttendanceTicketHolder::userId, Function.identity()));
        Map<Long, String> nickNames = userAdaptor.findNicknameMapByUserIds(
                winners.stream().map(EventWinner::userId).toList()
        );

        return winners.stream()
                .map(winner -> {
                    AttendanceTicketHolder holder = holderByUserId.get(winner.userId());
                    return AttendanceDrawWinner.builder()
                            .drawOrder(winner.drawOrder())
                            .userId(winner.userId())
                            .nickName(nickNames.get(winner.userId()))
                            .ticketCount(holder == null ? 0 : holder.ticketCount())
                            .totalAttendedDays(holder == null ? 0 : holder.totalAttendedDays())
                            .build();
                })
                .toList();
    }
}
