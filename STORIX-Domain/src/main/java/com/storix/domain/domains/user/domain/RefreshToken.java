package com.storix.domain.domains.user.domain;
import com.storix.common.utils.RedisKeyStatic;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(value = RedisKeyStatic.Hash.REFRESH_TOKEN)
@Getter
public class RefreshToken {
    @Id
    private Long id;

    @Indexed
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
