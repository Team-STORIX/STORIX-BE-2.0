package com.storix.api.domain.profile.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.domain.domains.profile.service.ProfileActivityService;
import com.storix.domain.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class ProfileActivityUseCase {

    private final ProfileActivityService profileActivityService;

    // 내가 쓴 게시글 조회
    public CustomResponse<Slice<ReaderBoardWithProfileInfo>> getReaderBoardList(Long userId, Pageable pageable) {

        Slice<ReaderBoardWithProfileInfo> result = profileActivityService.findAllReaderBoardList(userId, pageable);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_MY_BOARDS_LIST_LOAD_SUCCESS, result);
    }

    // 내가 쓴 댓글 조회
    public CustomResponse<Slice<ReaderBoardReplyInfoWithProfile>> getReaderBoardReplyList(Long userId, Pageable pageable) {

        Slice<ReaderBoardReplyInfoWithProfile> result = profileActivityService.findAllReaderBoardReplyList(userId, pageable);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_MY_BOARDS_REPLY_LIST_LOAD_SUCCESS, result);
    }

    // 내가 누른 좋아요 게시글 조회
    public CustomResponse<Slice<ReaderBoardWithProfileInfo>> getReaderBoardLikeList(Long userId, Pageable pageable) {

        Slice<ReaderBoardWithProfileInfo> result = profileActivityService.findAllReaderBoardsLikeList(userId, pageable);
        return CustomResponse.onSuccess(SuccessCode.PROFILE_MY_BOARDS_LIKE_LIST_LOAD_SUCCESS, result);
    }
}
