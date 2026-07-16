package com.storix.domain.domains.feed.domain;

import com.storix.domain.domains.plus.domain.DeletedBy;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "reader_board_reply",
        indexes = {
                @Index(name = "idx_reader_board_reply_board_id", columnList = "reader_board_id"),
                @Index(name = "idx_reader_board_reply_user_id", columnList = "user_id"),
                @Index(name = "idx_reader_board_reply_parent_id", columnList = "parent_reply_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderBoardReply extends BoardReply {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_board_id", nullable = false)
    private ReaderBoard board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_id")
    private ReaderBoardReply parentReply;

    @ToString.Exclude
    @OneToMany(mappedBy = "parentReply")
    @SQLRestriction("deleted = false")
    @BatchSize(size = 100) // 부모 댓글 페이징 후 답댓글은 배치 로드 (컬렉션 fetch join + 페이징의 인메모리 페이징 회피)
    private List<ReaderBoardReply> childReplies = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReaderBoardReplyLike> replyLikes = new ArrayList<>();

    @Builder
    public ReaderBoardReply(ReaderBoard board, Long userId, String comment, ReaderBoardReply parentReply) {
        this.board = board;
        this.userId = userId;
        this.comment = comment;
        this.parentReply = parentReply;
        this.depth = (parentReply != null) ? parentReply.getDepth() + 1 : 0;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedBy = DeletedBy.USER;
        this.comment = "삭제된 댓글입니다";
    }

    @Override
    public Long getBoardId() {
        return board.getId();
    }
}
