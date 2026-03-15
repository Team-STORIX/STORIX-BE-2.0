package com.storix.storix_api.domains.library.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.library.application.service.LibraryService;
import com.storix.storix_api.domains.library.dto.LibraryWrapperDto;
import com.storix.storix_api.domains.library.dto.StandardLibraryWorksInfo;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class LibraryUseCase {

    private final LibraryService libraryService;

    // 내 리뷰 작품 정보 조회
    public CustomResponse<LibraryWrapperDto<StandardLibraryWorksInfo>> getReviewedWorksInfo(Long userId, Pageable pageable) {

        int totalReviewCount = libraryService.getTotalReviewCount(userId);
        Slice<StandardLibraryWorksInfo> reviewedWorksInfos = libraryService.getReviewedWorksInfo(userId, pageable);

        LibraryWrapperDto<StandardLibraryWorksInfo> result =
                new LibraryWrapperDto<>(totalReviewCount, reviewedWorksInfos);

        return CustomResponse.onSuccess(SuccessCode.LIBRARY_WORKS_LOAD_SUCCESS, result);
    }

}
