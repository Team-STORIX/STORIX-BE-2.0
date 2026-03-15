package com.storix.storix_api.global.apiPayload.exception.feed;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class InvalidBoardRequestException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new InvalidBoardRequestException();

  private InvalidBoardRequestException() { super(ErrorCode.BOARD_INVALID_REQUEST);}
}
