package com.storix.domain.domains.user.service;

import com.storix.domain.domains.genrescore.service.TopGenreResolver;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        topGenreResolver.resolveAll(userIds)
                .forEach((userId, top) -> titleByUser.put(userId, top
                        .flatMap(t -> Title.resolve(t.genre(), t.score()))
                        .orElse(null)));

        // 2. 유저 조회
        Map<Long, User> users = userAdaptor.findUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 3. 칭호 반영 (dirty checking)
        titleByUser.forEach((userId, title) -> {
            User user = users.get(userId);
            if (user != null) user.changeTitle(title);
        });
    }

}
