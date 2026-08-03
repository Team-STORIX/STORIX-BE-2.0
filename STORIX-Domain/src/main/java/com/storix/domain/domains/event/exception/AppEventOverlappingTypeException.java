package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

// 전용 API가 붙는 타입(ATTENDANCE / STORY_CARD)은 같은 시점에 하나만 진행될 수 있다
public class AppEventOverlappingTypeException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new AppEventOverlappingTypeException();

    private AppEventOverlappingTypeException() { super(ErrorCode.ADMIN_APP_EVENT_OVERLAPPING_TYPE); }
}
