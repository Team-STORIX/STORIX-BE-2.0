package com.storix.storix_api.domains.user.domain;


import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(value = "onboardingToken")
@Getter
public class OnboardingToken {

    @Id
    private final String jti;

    private final OAuthProvider provider;
    private final String oid;

    @Indexed
    private final String onboardingToken;

    @TimeToLive
    private final Long ttl;

    @Builder
    public OnboardingToken(String jti, OAuthProvider provider, String oid, String onboardingToken, Long ttl) {
        this.jti = jti;
        this.provider = provider;
        this.oid = oid;
        this.onboardingToken = onboardingToken;
        this.ttl = ttl;
    }

}

