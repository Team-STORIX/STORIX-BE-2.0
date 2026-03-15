package com.storix.storix_api.domains.plus.domain;

import com.storix.storix_api.global.model.BaseTimeEntity;
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

}
