package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.dto.EventWinner;

import java.util.List;

public interface EventWinnerFinalizer {

    // 이 finalizer가 처리하는 이벤트 유형인지
    boolean supports(AppEvent event);

    // 당첨자는 앞에서부터 drawOrder 1, 2 … 순으로 담아 반환한다.
    // 후보가 winnerCount보다 적으면 있는 만큼만 반환한다.
    List<EventWinner> resolveWinners(AppEvent event, int winnerCount);
}
