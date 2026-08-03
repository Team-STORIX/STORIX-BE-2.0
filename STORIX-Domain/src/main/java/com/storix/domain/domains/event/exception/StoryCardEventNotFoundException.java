package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class StoryCardEventNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new StoryCardEventNotFoundException();

    private StoryCardEventNotFoundException() { super(ErrorCode.STORY_CARD_EVENT_NOT_FOUND); }
}
