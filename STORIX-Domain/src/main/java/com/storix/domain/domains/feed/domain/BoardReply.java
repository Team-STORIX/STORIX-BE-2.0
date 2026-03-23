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

    @Column(nullable = false)
    protected int depth = 0;

    @Column(name = "child_reply_count", nullable = false)
    protected int childReplyCount = 0;

    @Column(nullable = false)
    protected boolean deleted = false;

    public String getDisplayComment() {
        return deleted ? "삭제된 댓글입니다" : comment;
    }

    public abstract Long getBoardId();
}