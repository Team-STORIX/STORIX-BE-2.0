package com.storix.domain.domains.search.exception;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;

public class SearchReindexFailedException extends STORIXCodeException {

    public static final STORIXCodeException EXCEPTION = new SearchReindexFailedException();
    private SearchReindexFailedException() { super(ErrorCode.SEARCH_REINDEX_FAILED); }
}
