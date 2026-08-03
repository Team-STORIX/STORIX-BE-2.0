package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AttendanceTicketHolder;
import com.storix.domain.domains.event.dto.EventWinner;
import com.storix.domain.domains.event.service.AttendanceTicketPolicy;
import com.storix.domain.domains.event.service.WeightedTicketDraw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

// 출석 이벤트 당첨자 산출 — 응모권 수를 가중치로 하는 비복원 추첨
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceWinnerFinalizer implements EventWinnerFinalizer {

    // 추첨 결과가 예측 가능하면 안 되므로 SecureRandom 사용
    private static final RandomGenerator RANDOM = new SecureRandom();

    private final AttendanceCheckAdaptor attendanceCheckAdaptor;

    @Override
    public boolean supports(AppEvent event) {
        return event.getEventType() == AppEventType.ATTENDANCE;
    }

    @Override
    public List<EventWinner> resolveWinners(AppEvent event, int winnerCount) {
        List<AttendanceTicketHolder> candidates = AttendanceTicketPolicy.ticketHoldersOf(
                event, attendanceCheckAdaptor.findAttendeeCounts(event.getId())
        );
        List<AttendanceTicketHolder> drawn = WeightedTicketDraw.draw(candidates, winnerCount, RANDOM);

        // 추첨은 되돌릴 수 없으므로 어떤 모수로 뽑았는지 남긴다 (appEventId/appEventType 은 MDC)
        log.info(">>> [AttendanceEvent] 응모권 가중 추첨 완료 모수={}명/{}장, 요청={}명, 확정={}명",
                candidates.size(),
                candidates.stream().mapToInt(AttendanceTicketHolder::ticketCount).sum(),
                winnerCount, drawn.size());

        // 모수가 요청 인원보다 적어 상품이 남는 경우
        if (drawn.size() < winnerCount) {
            log.warn(">>> [AttendanceEvent] 추첨 모수 부족 - 요청 인원보다 적게 확정됨 요청={}명, 확정={}명",
                    winnerCount, drawn.size());
        }

        return IntStream.range(0, drawn.size())
                .mapToObj(index -> new EventWinner(drawn.get(index).userId(), index + 1))
                .toList();
    }
}
