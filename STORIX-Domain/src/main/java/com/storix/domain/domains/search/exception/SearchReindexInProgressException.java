package com.storix.domain.domains.search.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SearchReindexInProgressException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SearchReindexInProgressException();
    private SearchReindexInProgressException() { super(ErrorCode.SEARCH_REINDEX_IN_PROGRESS); }
}
