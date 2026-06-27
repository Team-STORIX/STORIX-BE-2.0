package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.UserSanctionHistory;
import com.storix.domain.domains.user.repository.UserSanctionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
