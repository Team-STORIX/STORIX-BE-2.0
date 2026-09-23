package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.notification.service.FeaturedNotificationHelper;
import com.storix.domain.domains.plus.dto.StandardReaderBoardInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodayFeedFeatureService {

    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final TodayFeedSelectionHelper todayFeedSelectionHelper;
    private final FeaturedNotificationHelper featuredNotificationHelper;

    public void selectAndNotify() {
        List<Long> selectedBoardIds = todayFeedSelectionHelper.reselect(LocalDateTime.now());
        List<StandardReaderBoardInfo> selected = readerFeedAdaptor.findStandardInfoByIds(selectedBoardIds);
        for (StandardReaderBoardInfo feed : selected) {
            try {
                featuredNotificationHelper.notifyTodayFeedIfFirst(feed.boardId(), feed.userId());
            } catch (Exception e) {
                log.error(">>> [TodayFeed] 피드 선정 알림 실패 feedId={}", feed.boardId(), e);
            }
        }
    }
}
