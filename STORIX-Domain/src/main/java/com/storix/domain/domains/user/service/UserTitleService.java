package com.storix.domain.domains.user.service;

import com.storix.domain.domains.genrescore.dto.TopGenreInfo;
import com.storix.domain.domains.genrescore.service.TopGenreResolver;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserTitleHistoryAdaptor;
import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.domain.UserTitleHistory;
import com.storix.domain.domains.user.event.TitleAcquiredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// 대표 장르 점수 기준으로 칭호를 산출하여 User 엔티티에 반영
// 장르 점수 집계(score_log -> raw_score) 직후 호출되어 함께 갱신됨
@Service
@RequiredArgsConstructor
public class UserTitleService {

    private static final int TITLE_ASSIGN_CHUNK_SIZE = 500;

    private final TopGenreResolver topGenreResolver;
    private final UserAdaptor userAdaptor;
    private final UserTitleHistoryAdaptor userTitleHistoryAdaptor;
    private final ApplicationEventPublisher eventPublisher;

    // 여러 유저의 칭호 일괄 갱신
    @Transactional
    public void assignTitles(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;

        // 1. 대상 유저 정리
        List<Long> targetUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .toList();

        // 2. 청크 단위 칭호 갱신
        for (int from = 0; from < targetUserIds.size(); from += TITLE_ASSIGN_CHUNK_SIZE) {
            int to = Math.min(from + TITLE_ASSIGN_CHUNK_SIZE, targetUserIds.size());
            assignTitleChunk(targetUserIds.subList(from, to));
        }
    }

    private void assignTitleChunk(List<Long> userIds) {
        // 1. 칭호 계산
        Map<Long, Title> titleByUser = new HashMap<>();
        List<UserTitleHistory> acquiredTitleHistories = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        topGenreResolver.resolveAll(userIds).forEach((userId, top) -> {
            titleByUser.put(userId, resolveTitle(top));
            acquiredTitleHistories.addAll(resolveAcquiredTitles(userId, top, now));
        });

        // 2. 유저 조회
        Map<Long, User> users = userAdaptor.findUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 3. 칭호 반영
        titleByUser.forEach((userId, title) -> {
            User user = users.get(userId);
            if (user != null) user.changeTitle(title);
        });

        // 4. 획득 칭호 저장 + 앱 모달 이벤트 발행
        List<UserTitleHistory> savedHistories = userTitleHistoryAdaptor.saveNewTitles(acquiredTitleHistories);
        savedHistories.forEach(history -> eventPublisher.publishEvent(
                new TitleAcquiredEvent(history.getUserId(), history.getTitle(), history.getAcquiredAt())
        ));
    }

    private Title resolveTitle(Optional<TopGenreInfo> top) {
        return top.flatMap(t -> Title.resolve(t.genre(), t.score())).orElse(null);
    }

    private Collection<UserTitleHistory> resolveAcquiredTitles(Long userId, Optional<TopGenreInfo> top, LocalDateTime acquiredAt) {
        return top.stream()
                .flatMap(info -> Title.resolveAcquired(info.genre(), info.score()).stream())
                .map(title -> userTitleHistoryAdaptor.create(userId, title, acquiredAt))
                .toList();
    }

}
