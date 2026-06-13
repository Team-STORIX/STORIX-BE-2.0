package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.UserTitleHistory;
import com.storix.domain.domains.user.repository.UserTitleHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTitleHistoryAdaptor {

    private static final int MYSQL_DUPLICATE_ENTRY = 1062; // ER_DUP_ENTRY

    private final UserTitleHistoryRepository userTitleHistoryRepository;

    // 획득 칭호 저장
    public void saveNewTitles(Collection<UserTitleHistory> histories) {
        if (histories == null || histories.isEmpty()) return;

        Set<Long> userIds = histories.stream()
                .map(UserTitleHistory::getUserId)
                .collect(Collectors.toSet());
        Set<Title> titles = histories.stream()
                .map(UserTitleHistory::getTitle)
                .collect(Collectors.toSet());

        Set<String> existingKeys = userTitleHistoryRepository.findAllByUserIdInAndTitleIn(userIds, titles).stream()
                .map(history -> key(history.getUserId(), history.getTitle()))
                .collect(Collectors.toSet());

        List<UserTitleHistory> newHistories = histories.stream()
                .filter(history -> !existingKeys.contains(key(history.getUserId(), history.getTitle())))
                .toList();
        if (newHistories.isEmpty()) return;

        try {
            userTitleHistoryRepository.saveAll(newHistories);
        } catch (DataIntegrityViolationException e) {
            // 중복 획득(unique 충돌)만 무시, 그 외는 전파
            if (!isDuplicateKey(e)) throw e;
        }
    }

    private boolean isDuplicateKey(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && sql.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                return true;
            }
        }
        return false;
    }

    public UserTitleHistory create(Long userId, Title title, LocalDateTime acquiredAt) {
        return UserTitleHistory.of(userId, title, acquiredAt);
    }

    private String key(Long userId, Title title) {
        return userId + ":" + title.name();
    }
}
