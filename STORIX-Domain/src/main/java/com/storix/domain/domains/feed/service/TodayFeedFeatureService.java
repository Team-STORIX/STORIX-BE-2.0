package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.notification.service.FeaturedNotificationService;
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

    private static final int TRENDING_WINDOW_HOURS = 24;

    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final FeaturedNotificationService featuredNotificationService;

    public void selectAndNotify() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(TRENDING_WINDOW_HOURS);
        List<StandardReaderBoardInfo> selected = readerFeedAdaptor.findTop3TrendingFeed(threshold);
        for (StandardReaderBoardInfo feed : selected) {
            try {
                featuredNotificationService.notifyTodayFeedIfFirst(feed.boardId(), feed.userId());
            } catch (Exception e) {
                log.error(">>> [TodayFeed] 피드 선정 알림 실패 feedId={}", feed.boardId(), e);
            }
        }
    }
}
