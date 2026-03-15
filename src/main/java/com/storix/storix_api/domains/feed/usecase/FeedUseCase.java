package com.storix.storix_api.domains.feed.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.feed.dto.BoardWrapperDto;
import com.storix.storix_api.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.storix_api.domains.feed.service.FeedService;
import com.storix.storix_api.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.storix_api.domains.works.dto.SlicedWorksInfo;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class FeedUseCase {

    private final FeedService feedService;

    // 전체 게시글 조회
    public CustomResponse<Slice<ReaderBoardWithProfileInfo>> getAllReaderBoard(Long userId, Pageable pageable) {

        Slice<ReaderBoardWithProfileInfo> result = feedService.getAllReaderBoard(userId, pageable);
        return CustomResponse.onSuccess(SuccessCode.FEED_ALL_READER_BOARD_LOAD_SUCCESS, result);
    }

    // 관심 작품 리스트 조회
    public CustomResponse<Slice<SlicedWorksInfo>> getSlicedFavoriteWorksInfo(Long userId, Pageable pageable) {

        Slice<SlicedWorksInfo> result = feedService.findFavoriteWorksList(userId, pageable);
        return CustomResponse.onSuccess(SuccessCode.FEED_FAVORITE_WORKS_INFO_LOAD_SUCCESS, result);
    }

    // 관심 작품 관련 게시글 조회
    public CustomResponse<Slice<ReaderBoardWithProfileInfo>> getReaderBoard(Long userId, Long worksId, Pageable pageable) {

        Slice<ReaderBoardWithProfileInfo> result = feedService.findAllReaderBoardFeedByWorksId(userId, worksId, pageable);
        return CustomResponse.onSuccess(SuccessCode.FEED_WORKS_READER_BOARD_LOAD_SUCCESS, result);
    }

    // 단건 게시글 조회
    public CustomResponse<BoardWrapperDto<ReaderBoardReplyInfoWithProfile>> getReaderBoardDetail(Long userId, Long boardId, Pageable pageable) {

        BoardWrapperDto<ReaderBoardReplyInfoWithProfile> result = feedService.findReaderBoardDetail(userId, boardId, pageable);
        return CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_LOAD_SUCCESS, result);
    }

}
