package com.storix.domain.domains.feed.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.plus.domain.DeletedBy;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "deleted_by")
    protected DeletedBy deletedBy;

    public String getDisplayComment() {
        return deleted ? "삭제된 댓글입니다" : comment;
    }

    public boolean softDeleteByAdmin() {
        if (this.deleted) return false;
        this.deleted = true;
        this.deletedBy = DeletedBy.ADMIN;
        return true;
    }

    // 필드 추가 이전 삭제 데이터는 deletedBy가 없으므로 USER로 간주
    public DeletedBy getDeletedBy() {
        return (deleted && deletedBy == null) ? DeletedBy.USER : deletedBy;
    }

    public abstract Long getBoardId();
}
