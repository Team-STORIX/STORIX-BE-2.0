package com.storix.storix_api.global.apiPayload.exception.feed;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class BoardReplyNotFoundException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new BoardReplyNotFoundException();

  private BoardReplyNotFoundException() { super(ErrorCode.BOARD_REPLY_NOT_FOUND);}
}
