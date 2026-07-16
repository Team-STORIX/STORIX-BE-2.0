package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.UserBlock;
import com.storix.domain.domains.user.dto.BlockUserCommand;
import com.storix.domain.domains.user.exception.block.DuplicateUserBlockException;
import com.storix.domain.domains.user.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserBlockAdaptor {

    private final UserBlockRepository userBlockRepository;

    public void saveBlock(BlockUserCommand cmd) {
        try {
            UserBlock userBlock = cmd.toEntity();
            userBlockRepository.save(userBlock);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateUserBlockException.EXCEPTION;
        }
    }

    public boolean isBlocked(Long blockerId, Long blockedUserId) {
        return userBlockRepository.existsByBlockerIdAndBlockedUserId(blockerId, blockedUserId);
    }

    public List<Long> findBlockedUserIds(Long blockerId) {
        return userBlockRepository.findAllByBlockerId(blockerId)
                .stream()
                .map(UserBlock::getBlockedUserId)
                .toList();
    }
}
