package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.dto.BlockUserCommand;
import com.storix.domain.domains.user.exception.block.SelfBlockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final UserAdaptor userAdaptor;
    private final UserBlockAdaptor userBlockAdaptor;

    @Transactional
    public void blockUser(Long blockerId, Long blockedUserId) {
        if (blockerId.equals(blockedUserId)) {
            throw SelfBlockException.EXCEPTION;
        }

        userAdaptor.findUserById(blockedUserId);

        BlockUserCommand cmd = new BlockUserCommand(blockerId, blockedUserId);
        userBlockAdaptor.saveBlock(cmd);
    }
}
