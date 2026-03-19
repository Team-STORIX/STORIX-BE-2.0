package com.storix.domain.domains.user.domain;

import com.storix.domain.domains.works.domain.Genre;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.Set;

@RedisHash(value = "developerSignupPending")
@Getter
public class DeveloperSignupPending {

    @Id
    private final String pendingId;

    private final String nickName;
    private final Set<Genre> favoriteGenreList;
    private final Set<Long> favoriteWorksIdList;

    @TimeToLive
    private final Long ttl;

    @Builder
    public DeveloperSignupPending(String pendingId, String nickName, Set<Genre> favoriteGenreList, Set<Long> favoriteWorksIdList, Long ttl) {
        this.pendingId = pendingId;
        this.nickName = nickName;
        this.favoriteGenreList = favoriteGenreList;
        this.favoriteWorksIdList = favoriteWorksIdList;
        this.ttl = ttl;
    }
}
