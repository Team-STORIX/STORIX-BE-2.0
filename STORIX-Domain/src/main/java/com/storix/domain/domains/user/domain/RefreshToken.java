package com.storix.domain.domains.user.domain;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "refreshToken")
@Getter
public class RefreshToken {
    @Id
    private Long id;

    private String refreshToken;

    @TimeToLive
    private Long ttl;

    @Builder
    public RefreshToken(Long id, String refreshToken, Long ttl) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.ttl = ttl;
    }

}
