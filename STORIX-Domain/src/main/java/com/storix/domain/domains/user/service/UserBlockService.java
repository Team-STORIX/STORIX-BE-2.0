package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.dto.BlockUserCommand;
import com.storix.domain.domains.user.exception.block.SelfBlockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    @Transactional
    public int unblockUser(Long blockerId, Long blockedUserId) {
        int deleted = userBlockAdaptor.deleteBlock(blockerId, blockedUserId);

        log.info(">>> [Block] 테스트 차단 해제 blockerId={} blockedUserId={} deleted={}",
                blockerId, blockedUserId, deleted);
        return deleted;
    }
}
