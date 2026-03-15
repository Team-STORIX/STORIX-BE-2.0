package com.storix.storix_api.domains.home.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.feed.service.FeedService;
import com.storix.storix_api.domains.home.dto.SlicedReaderBoardWithProfileInfo;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class TodayFeedUseCase {

    private final FeedService feedService;

    // 오늘의 피드
    public CustomResponse<List<SlicedReaderBoardWithProfileInfo>> getTodayTrendingFeeds(AuthUserDetails authUserDetails) {

        Long userId = (authUserDetails != null) ? authUserDetails.getUserId() : null;

        List<SlicedReaderBoardWithProfileInfo> result = feedService.findTodayTrendingFeeds(userId);
        return CustomResponse.onSuccess(SuccessCode.HOME_TODAY_FEED_LOAD_SUCCESS, result);
    }

}
