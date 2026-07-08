package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.notification.adaptor.FeaturedNotificationDedupAdaptor;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.plus.dto.StandardReaderBoardInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodayFeedFeatureService {

    private static final int TRENDING_WINDOW_HOURS = 24;

    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final FeaturedNotificationDedupAdaptor dedupAdaptor;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public void selectAndNotify() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(TRENDING_WINDOW_HOURS);
        List<StandardReaderBoardInfo> selected = readerFeedAdaptor.findTop3TrendingFeed(threshold);
        for (StandardReaderBoardInfo feed : selected) {
            if (dedupAdaptor.markFeedIfFirstToday(feed.boardId())) {
                notificationPublisher.publish(NotificationEvent.todayFeed(feed.userId(), feed.boardId()));
            }
        }
    }
}
