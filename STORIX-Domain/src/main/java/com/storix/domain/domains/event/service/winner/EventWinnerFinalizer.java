package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.domain.AppEvent;

import java.util.List;

public interface EventWinnerFinalizer {

    // 이 finalizer가 처리하는 이벤트 유형인지
    boolean supports(AppEvent event);

    // 종료 시점 당첨자 userId 산출
    List<Long> resolveWinnerUserIds(AppEvent event);
}
