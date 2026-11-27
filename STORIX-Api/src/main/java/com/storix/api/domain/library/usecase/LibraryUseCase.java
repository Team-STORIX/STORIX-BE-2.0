package com.storix.api.domain.library.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.library.service.LibraryCountCacheHelper;
import com.storix.domain.domains.library.service.LibraryService;
import com.storix.domain.domains.library.dto.LibraryWrapperDto;
import com.storix.domain.domains.library.dto.StandardLibraryWorksInfo;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class LibraryUseCase {

    private final LibraryService libraryService;
    private final LibraryCountCacheHelper libraryCountCacheHelper;

    // 내 리뷰 작품 정보 조회
    public CustomResponse<LibraryWrapperDto<StandardLibraryWorksInfo>> getReviewedWorksInfo(Long userId, Pageable pageable) {

        Integer totalReviewCount = libraryCountCacheHelper.findReviewCount(userId);
        if (totalReviewCount == null) {
            totalReviewCount = libraryService.getTotalReviewCount(userId);
            libraryCountCacheHelper.saveReviewCount(userId, totalReviewCount);
        }
        Slice<StandardLibraryWorksInfo> reviewedWorksInfos = libraryService.getReviewedWorksInfo(userId, pageable);

        LibraryWrapperDto<StandardLibraryWorksInfo> result =
                new LibraryWrapperDto<>(totalReviewCount, reviewedWorksInfos);

        return CustomResponse.onSuccess(SuccessCode.LIBRARY_WORKS_LOAD_SUCCESS, result);
    }

}
