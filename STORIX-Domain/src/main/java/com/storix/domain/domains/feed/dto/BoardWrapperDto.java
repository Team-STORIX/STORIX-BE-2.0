package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.profile.dto.ReaderBoardWithProfileInfo;
import org.springframework.data.domain.Slice;

public record BoardWrapperDto <T> (
        ReaderBoardWithProfileInfo board,
        Slice<T> comment
) {
}
