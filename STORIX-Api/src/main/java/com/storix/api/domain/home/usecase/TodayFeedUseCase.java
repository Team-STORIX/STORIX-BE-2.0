package com.storix.api.domain.home.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.feed.service.FeedService;
import com.storix.domain.domains.feed.service.TodayFeedSelectionHelper;
import com.storix.domain.domains.feed.dto.SlicedReaderBoardWithProfileInfo;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class TodayFeedUseCase {

    private final FeedService feedService;
    private final TodayFeedSelectionHelper todayFeedSelectionHelper;

    // 오늘의 피드
    public CustomResponse<List<SlicedReaderBoardWithProfileInfo>> getTodayTrendingFeeds(AuthUserDetails authUserDetails) {

        List<Long> selectedBoardIds = todayFeedSelectionHelper.loadOrSelect(LocalDateTime.now());
        List<SlicedReaderBoardWithProfileInfo> result = feedService.findTodayTrendingFeeds(authUserDetails.getUserId(), selectedBoardIds);
        return CustomResponse.onSuccess(SuccessCode.HOME_TODAY_FEED_LOAD_SUCCESS, result);
    }

}
