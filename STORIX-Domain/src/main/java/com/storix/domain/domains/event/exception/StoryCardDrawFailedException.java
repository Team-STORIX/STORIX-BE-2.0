package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

// insert 직후 재조회에도 행이 없는 경우 (정상 경로에서는 발생하지 않는다)
public class StoryCardDrawFailedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new StoryCardDrawFailedException();

    private StoryCardDrawFailedException() { super(ErrorCode.STORY_CARD_DRAW_FAILED); }
}
