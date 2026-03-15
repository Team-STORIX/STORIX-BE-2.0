package com.storix.storix_api.global.apiPayload.exception.works;

import com.storix.storix_api.global.apiPayload.code.ErrorCode;
import com.storix.storix_api.global.apiPayload.exception.STORIXCodeException;

public class UnknownWorksException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new UnknownWorksException();

  private UnknownWorksException() { super(ErrorCode.WORKS_NOT_FOUND);}
}
