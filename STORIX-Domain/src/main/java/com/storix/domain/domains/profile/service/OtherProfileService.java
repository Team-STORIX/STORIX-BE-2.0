package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.profile.exception.SelfProfileRequestException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.exception.block.BlockedUserContentException;
import com.storix.domain.domains.user.exception.me.UnknownUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtherProfileService {

    private final UserAdaptor userAdaptor;
    private final UserBlockAdaptor userBlockAdaptor;

    @Transactional(readOnly = true)
    public User getViewableUser(Long viewerId, Long targetUserId) {
        if (targetUserId.equals(viewerId)) {
            throw SelfProfileRequestException.EXCEPTION;
        }

        User target = userAdaptor.findUserById(targetUserId);
        if (target.getAccountState() != AccountState.NORMAL) {
            throw UnknownUserException.EXCEPTION;
        }

        if (userBlockAdaptor.isBlocked(viewerId, targetUserId)) {
            throw BlockedUserContentException.EXCEPTION;
        }

        return target;
    }

    @Transactional(readOnly = true)
    public boolean isLibraryPublic(Long viewerId, Long targetUserId) {
        return getViewableUser(viewerId, targetUserId).isLibraryPublic();
    }
}
