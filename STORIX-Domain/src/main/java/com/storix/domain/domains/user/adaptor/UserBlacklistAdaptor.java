package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.UserBlacklist;
import com.storix.domain.domains.user.domain.UserBlacklist.BlockReason;
import com.storix.domain.domains.user.repository.UserBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserBlacklistAdaptor {

    private final UserBlacklistRepository userBlacklistRepository;

    // 정지 유저 등록 — TTL = 정지 만료까지 남은 초, 자동 소멸 시 차단 해제
    public void blockSuspended(Long userId, LocalDateTime suspendedUntil) {
        long remainingSeconds = Duration.between(LocalDateTime.now(), suspendedUntil).getSeconds();
        if (remainingSeconds <= 0) return;

        userBlacklistRepository.save(UserBlacklist.builder()
                .userId(userId)
                .reason(BlockReason.SUSPENDED)
                .ttl(remainingSeconds)
                .build());
    }

    // 탈퇴 유저 등록 — TTL 없음 (영구 차단)
    public void blockDeleted(Long userId) {
        userBlacklistRepository.save(UserBlacklist.builder()
                .userId(userId)
                .reason(BlockReason.DELETED)
                .ttl(null)
                .build());
    }

    public Optional<BlockReason> getBlockReason(Long userId) {
        return userBlacklistRepository.findById(userId)
                .map(UserBlacklist::getReason);
    }
}
