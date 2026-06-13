package com.storix.domain.domains.plus.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Board extends BaseTimeEntity {

    @Column(name = "user_id")
    protected Long userId;

    @Column(name = "is_works_selected")
    protected boolean isWorksSelected;

    @Column(name = "works_id")
    protected Long worksId;

    @Column(length = 300, nullable = false)
    protected String content;

    @Column(name = "like_count", nullable = false)
    protected int likeCount = 0;

    @Column(name = "reply_count", nullable = false)
    protected int replyCount = 0;

    @Column(nullable = false)
    protected boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "deleted_by")
    protected DeletedBy deletedBy;

    public boolean softDeleteByAdmin() {
        if (this.deleted) return false;
        this.deleted = true;
        this.deletedBy = DeletedBy.ADMIN;
        return true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    // 필드 추가 이전 삭제 데이터는 deletedBy가 없으므로 USER로 간주
    public DeletedBy getDeletedBy() {
        return (deleted && deletedBy == null) ? DeletedBy.USER : deletedBy;
    }

}
