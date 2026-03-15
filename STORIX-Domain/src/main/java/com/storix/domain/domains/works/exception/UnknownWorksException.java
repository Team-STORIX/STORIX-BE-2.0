package com.storix.domain.domains.works.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class UnknownWorksException extends STORIXCodeException {

  public static final STORIXCodeException EXCEPTION = new UnknownWorksException();

  private UnknownWorksException() { super(ErrorCode.WORKS_NOT_FOUND);}
}
