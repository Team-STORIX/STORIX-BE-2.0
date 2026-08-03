package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

// 전용 API가 붙는 타입은 하루가 바뀌는 기준 시각에 이벤트 기간 경계를 맞춰야 한다 (출석 00:00 / 스토리 카드 06:00)
public class AppEventInvalidPeriodBoundaryException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventInvalidPeriodBoundaryException();

    private AppEventInvalidPeriodBoundaryException() { super(ErrorCode.ADMIN_APP_EVENT_INVALID_PERIOD_BOUNDARY); }
}
