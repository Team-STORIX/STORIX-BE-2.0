package com.storix.domain.domains.user.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "adminSignupPending")
@Getter
public class AdminSignupPending {

    @Id
    private final String pendingId;

    private final String email;
    private final String encodedPassword;
    private final String nickName;

    @TimeToLive
    private final Long ttl;

    @Builder
    public AdminSignupPending(String pendingId, String email, String encodedPassword, String nickName, Long ttl) {
        this.pendingId = pendingId;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.nickName = nickName;
        this.ttl = ttl;
    }
}
