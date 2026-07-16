package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.UserHistory;
import com.storix.domain.domains.user.domain.UserTermHistory;
import com.storix.domain.domains.user.repository.UserHistoryRepository;
import com.storix.domain.domains.user.repository.UserTermHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHistoryAdaptor {

    private final UserHistoryRepository userHistoryRepository;
    private final UserTermHistoryRepository userTermHistoryRepository;

    // 사용자 이력 저장
    public UserHistory save(UserHistory userHistory) {
        return userHistoryRepository.save(userHistory);
    }

    // 사용자 약관 동의/철회 이력 저장
    public UserTermHistory saveUserTermHistory(UserTermHistory userTermHistory) {
        return userTermHistoryRepository.save(userTermHistory);
    }
}
