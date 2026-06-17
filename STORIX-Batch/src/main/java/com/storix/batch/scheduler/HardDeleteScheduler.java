package com.storix.batch.scheduler;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HardDeleteScheduler {

    private static final int RETENTION_YEARS = 5;

    private final BoardAdaptor boardAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final ChatAdaptor chatAdaptor;
    private final UserAdaptor userAdaptor;

    /**
     * 매일 새벽 4시 실행.
     * soft-delete 후 5년이 경과한 레코드를 테이블별로 순차 hard-delete 한다.
     * 댓글(자식) → 게시물(부모) 순서로 삭제해 FK 위반을 방지한다.
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void hardDeleteExpiredRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(RETENTION_YEARS);
        log.info(">>>> [HardDeleteScheduler] 시작 — cutoff: {}", cutoff);

        int replies  = readerFeedAdaptor.hardDeleteRepliesBefore(cutoff);
        int boards   = boardAdaptor.hardDeleteBefore(cutoff);
        int reviews  = reviewAdaptor.hardDeleteBefore(cutoff);
        int messages = chatAdaptor.hardDeleteBefore(cutoff);
        int users    = userAdaptor.hardDeleteBefore(cutoff);

        log.info(">>>> [HardDeleteScheduler] 완료 — 댓글: {}건, 게시물: {}건, 리뷰: {}건, 채팅: {}건, 유저: {}건",
                replies, boards, reviews, messages, users);
    }
}
