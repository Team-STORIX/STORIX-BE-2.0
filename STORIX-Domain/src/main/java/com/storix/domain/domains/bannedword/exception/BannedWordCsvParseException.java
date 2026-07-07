package com.storix.domain.domains.bannedword.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class BannedWordCsvParseException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new BannedWordCsvParseException();

    private BannedWordCsvParseException() {
        super(ErrorCode.BANNED_WORD_CSV_PARSE_ERROR);
    }
}
