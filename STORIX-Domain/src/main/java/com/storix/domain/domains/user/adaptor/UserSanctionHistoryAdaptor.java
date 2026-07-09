package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.UserSanctionHistory;
import com.storix.domain.domains.user.repository.UserSanctionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserSanctionHistoryAdaptor {

    private final UserSanctionHistoryRepository userSanctionHistoryRepository;

    public UserSanctionHistory save(UserSanctionHistory history) {
        return userSanctionHistoryRepository.save(history);
    }

    public List<UserSanctionHistory> findAllByUserId(Long userId) {
        return userSanctionHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<UserSanctionHistory> findPageByUserId(Long userId, Pageable pageable) {
        return userSanctionHistoryRepository.findPageByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public int deleteExpiredBefore(LocalDateTime cutoff) {
        return userSanctionHistoryRepository.deleteExpiredBefore(cutoff);
    }
}
