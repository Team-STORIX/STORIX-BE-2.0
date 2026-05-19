package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.UserHistory;
import com.storix.domain.domains.user.repository.UserHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHistoryAdaptor {

    private final UserHistoryRepository userHistoryRepository;

    // 사용자 이력 저장
    public UserHistory save(UserHistory userHistory) {
        return userHistoryRepository.save(userHistory);
    }
}
