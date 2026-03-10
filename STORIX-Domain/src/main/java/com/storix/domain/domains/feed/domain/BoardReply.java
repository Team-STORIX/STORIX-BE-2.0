package com.storix.domain.domains.feed.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BoardReply extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "user_id", nullable = false)
    protected Long userId;

    @Column(length = 300, nullable = false)
    protected String comment;

    @Column(name = "like_count", nullable = false)
    protected int likeCount = 0;

    public abstract Long getBoardId();
}