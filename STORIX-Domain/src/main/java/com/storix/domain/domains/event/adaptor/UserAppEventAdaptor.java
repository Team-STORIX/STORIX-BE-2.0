package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.UserAppEvent;
import com.storix.domain.domains.event.domain.UserAppEventStatus;
import com.storix.domain.domains.event.exception.UserAppEventForbiddenException;
import com.storix.domain.domains.event.exception.UserAppEventNotFoundException;
import com.storix.domain.domains.event.repository.UserAppEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAppEventAdaptor {

    private final UserAppEventRepository userAppEventRepository;

    public UserAppEvent save(UserAppEvent event) {
        return userAppEventRepository.save(event);
    }

    public UserAppEvent findById(Long eventId) {
        return userAppEventRepository.findById(eventId)
                .orElseThrow(() -> UserAppEventNotFoundException.EXCEPTION);
    }

    // 존재 확인 + 소유권 검증 후 반환
    public UserAppEvent findByIdAndOwner(Long eventId, Long userId) {
        UserAppEvent event = findById(eventId);
        if (!event.getUserId().equals(userId)) {
            throw UserAppEventForbiddenException.EXCEPTION;
        }
        return event;
    }

    public List<UserAppEvent> findPendingByUserId(Long userId, Pageable pageable) {
        return userAppEventRepository.findAllByUserIdAndStatusOrderByIdAsc(
                userId, UserAppEventStatus.PENDING, pageable
        );
    }
}
