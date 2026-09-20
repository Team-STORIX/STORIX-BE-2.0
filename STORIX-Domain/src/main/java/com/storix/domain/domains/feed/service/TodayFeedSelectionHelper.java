package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.adaptor.TodayFeedSnapshotAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodayFeedSelectionHelper {

    public static final LocalTime SELECTION_TIME = LocalTime.of(8, 0);

    private static final int SELECTION_SIZE = 3;
    private static final int CANDIDATE_WINDOW_HOURS = 24;

    // 스냅샷 키와 같은 모양으로 찍어야 로그에서 본 날짜로 바로 Redis 를 뒤질 수 있다
    private static final DateTimeFormatter SELECTION_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final TodayFeedSnapshotAdaptor snapshotAdaptor;

    // 08시를 하루 경계로 삼는다. 자정에 날짜가 바뀌어도 선정일은 그대로여야 하루 1회 갱신이 지켜진다
    public static LocalDate selectionDateOf(LocalDateTime now) {
        return now.toLocalTime().isBefore(SELECTION_TIME)
                ? now.toLocalDate().minusDays(1)
                : now.toLocalDate();
    }

    // 조회 경로. 오늘 선정이 남아있으면 그대로 쓴다
    public List<Long> loadOrSelect(LocalDateTime now) {
        LocalDate selectionDate = selectionDateOf(now);
        return snapshotAdaptor.find(selectionDate)
                .orElseGet(() -> selectAndSave(selectionDate));
    }

    // 08시 배치. 남아있던 선정을 덮어쓴다
    public List<Long> reselect(LocalDateTime now) {
        return selectAndSave(selectionDateOf(now));
    }

    private List<Long> selectAndSave(LocalDate selectionDate) {
        LocalDateTime threshold = LocalDateTime.of(selectionDate, SELECTION_TIME)
                .minusHours(CANDIDATE_WINDOW_HOURS);
        String seed = selectionDate.format(SELECTION_DATE);

        List<Long> boardIds = readerFeedAdaptor.findTodayFeedCandidateIds(
                threshold, seed, SELECTION_SIZE);

        snapshotAdaptor.save(selectionDate, boardIds);

        log.atInfo()
                .addKeyValue("selectionDate", seed)
                .addKeyValue("boardIds", boardIds)
                .log(">>> [TodayFeed] 선정 완료");

        return boardIds;
    }
}
