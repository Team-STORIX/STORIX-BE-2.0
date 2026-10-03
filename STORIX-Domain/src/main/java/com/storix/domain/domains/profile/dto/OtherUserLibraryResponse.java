package com.storix.domain.domains.profile.dto;

import com.storix.domain.domains.library.dto.StandardLibraryWorksInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

public record OtherUserLibraryResponse(

        @Schema(description = "서재 공개 여부. false 면 totalReviewCount 는 0, result 는 빈 목록")
        boolean isPublic,

        int totalReviewCount,

        Slice<StandardLibraryWorksInfo> result
) {
    public static OtherUserLibraryResponse ofPublic(int totalReviewCount, Slice<StandardLibraryWorksInfo> result) {
        return new OtherUserLibraryResponse(true, totalReviewCount, result);
    }

    public static OtherUserLibraryResponse ofPrivate(Pageable pageable) {
        return new OtherUserLibraryResponse(false, 0, new SliceImpl<>(List.of(), pageable, false));
    }
}
