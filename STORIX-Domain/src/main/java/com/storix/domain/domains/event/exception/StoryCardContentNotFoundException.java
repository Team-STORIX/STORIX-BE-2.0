package com.storix.domain.domains.event.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

// 배정된 장르의 한마디/작품 풀이 비어 있거나 뽑기 결과가 참조하는 콘텐츠가 사라진 경우
public class StoryCardContentNotFoundException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new StoryCardContentNotFoundException();

    private StoryCardContentNotFoundException() { super(ErrorCode.STORY_CARD_CONTENT_NOT_FOUND); }
}
