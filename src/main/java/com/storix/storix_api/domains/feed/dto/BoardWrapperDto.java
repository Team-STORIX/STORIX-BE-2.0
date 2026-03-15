package com.storix.storix_api.domains.feed.dto;

import com.storix.storix_api.domains.profile.dto.ReaderBoardWithProfileInfo;
import org.springframework.data.domain.Slice;

public record BoardWrapperDto <T> (
        ReaderBoardWithProfileInfo board,
        Slice<T> comment
) {
}
