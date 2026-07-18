package com.storix.batch.scheduler;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.dto.BoardHardDeleteResult;
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

    /**
     * 매일 새벽 4시 실행.
     * soft-delete 후 5년이 경과한 콘텐츠 레코드를 테이블별로 순차 hard-delete 한다.
     * 자식(좋아요/댓글/이미지) → 부모(게시물) 순서로 삭제해 FK 위반을 방지한다.
     * 게시물의 첨부 이미지는 S3CleanupEvent 로 발행되어 트랜잭션 커밋 후 S3 에서 정리된다.
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void hardDeleteExpiredRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(RETENTION_YEARS);
        log.info(">>>> [HardDeleteScheduler] 시작 — cutoff: {}", cutoff);

        int replies  = readerFeedAdaptor.hardDeleteRepliesBefore(cutoff);
        BoardHardDeleteResult boards = boardAdaptor.hardDeleteBoardsBefore(cutoff);
        int reviews  = reviewAdaptor.hardDeleteBefore(cutoff);
        int messages = chatAdaptor.hardDeleteBefore(cutoff);

        log.info(">>>> [HardDeleteScheduler] 완료 — 댓글: {}건, 게시물: {}건(이미지 {}건), 리뷰: {}건, 채팅: {}건",
                replies, boards.boardCount(), boards.imageCount(), reviews, messages);
    }
}
