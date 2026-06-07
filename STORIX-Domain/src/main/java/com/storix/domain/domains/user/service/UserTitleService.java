package com.storix.domain.domains.user.service;

import com.storix.domain.domains.genrescore.service.TopGenreResolver;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// 대표 장르 점수 기준으로 칭호를 산출하여 User 엔티티에 반영
// 장르 점수 집계(score_log -> raw_score) 직후 호출되어 함께 갱신됨
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTitleService {

    private final TopGenreResolver topGenreResolver;
    private final UserAdaptor userAdaptor;

    // 여러 유저의 칭호를 한 트랜잭션에서 일괄 갱신.
    // (1) 읽기 단계에서 칭호를 모두 계산한 뒤 (2) 쓰기 단계에서 한꺼번에 변경 ->
    // 변경 사이에 쿼리가 끼지 않아 commit 시점에 dirty UPDATE 들이 JDBC 배치로 flush 된다.
    @Transactional
    public void assignTitles(Collection<Long> userIds) {
        // (1) 읽기 단계: 유저별 칭호 계산 (한 명 실패는 건너뜀)
        Map<Long, Title> titleByUser = new HashMap<>();
        for (Long userId : userIds) {
            try {
                titleByUser.put(userId, resolveTitle(userId));
            } catch (Exception e) {
                log.warn(">>> [UserTitle] 칭호 계산 실패 userId={}", userId, e);
            }
        }

        // (2) 쓰기 단계: 대상 유저를 한 번에 로드 후 변경 (중간 쿼리 없음)
        Map<Long, User> users = userAdaptor.findUsersByIds(titleByUser.keySet()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        titleByUser.forEach((userId, title) -> {
            User user = users.get(userId);
            if (user != null) user.changeTitle(title);
        });
    }

    // 대표 장르 점수 기준 칭호 산출. 미진입(칭호 없음)이면 null
    public Title resolveTitle(Long userId) {
        return topGenreResolver.resolve(userId)
                .flatMap(top -> Title.resolve(top.genre(), top.score()))
                .orElse(null);
    }
}
