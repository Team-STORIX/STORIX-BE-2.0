package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.application.port.LoadUserPort;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.repository.UserRepository;
import com.storix.domain.domains.user.exception.me.UnknownUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdaptor implements LoadUserPort {

    private final UserRepository userRepository;

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UnknownUserException.EXCEPTION);
    }

    @Override
    public Boolean findIsAdultVerifiedById(Long userId) {
        Boolean result = userRepository.findIsAdultVerifiedById(userId);
        return result != null ? result : false;
    }
}
