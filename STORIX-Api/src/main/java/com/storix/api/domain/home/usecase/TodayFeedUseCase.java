package com.storix.api.domain.home.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.feed.service.FeedService;
import com.storix.domain.domains.feed.dto.SlicedReaderBoardWithProfileInfo;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
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
