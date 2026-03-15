package com.storix.storix_api.domains.user.adaptor;

import com.storix.storix_api.domains.user.application.port.LoadUserPort;
import com.storix.storix_api.domains.user.domain.Role;
import com.storix.storix_api.domains.user.domain.User;
import com.storix.storix_api.domains.user.repository.UserRepository;
import com.storix.storix_api.global.apiPayload.exception.user.UnknownUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdaptor implements LoadUserPort {

    private final UserRepository userRepository;

    @Override
    public Slice<User> searchArtists(String keyword, Pageable pageable) {

        // Role이 ARTIST인 유저 중에서 닉네임 검색
        return userRepository.findByRoleAndNickNameContaining(
                Role.ARTIST, keyword, pageable
        );
    }

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
