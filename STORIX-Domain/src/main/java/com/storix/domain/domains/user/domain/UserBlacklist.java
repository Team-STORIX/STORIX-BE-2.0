package com.storix.domain.domains.user.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "userBlacklist")
@Getter
public class UserBlacklist {

    @Id
    private Long userId;

    private BlockReason reason;

    @TimeToLive
    private Long ttl; // 초 단위; null = 무기한 (DELETED 전용)

    @Builder
    public UserBlacklist(Long userId, BlockReason reason, Long ttl) {
        this.userId = userId;
        this.reason = reason;
        this.ttl = ttl;
    }

    public enum BlockReason {
        SUSPENDED, DELETED
    }
}
