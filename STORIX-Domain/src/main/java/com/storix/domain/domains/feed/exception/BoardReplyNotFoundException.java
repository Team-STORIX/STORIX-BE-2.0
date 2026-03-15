package com.storix.domain.domains.feed.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BoardReplyNotFoundException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new BoardReplyNotFoundException();

  private BoardReplyNotFoundException() { super(ErrorCode.BOARD_REPLY_NOT_FOUND);}
}
