package com.storix.storix_api.domains.feed.domain;

import com.storix.storix_api.domains.plus.domain.ReaderBoard;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "reader_board_reply",
        indexes = {
                @Index(name = "idx_reader_board_reply_board_id", columnList = "reader_board_id"),
                @Index(name = "idx_reader_board_reply_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderBoardReply extends BoardReply {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_board_id", nullable = false)
    private ReaderBoard board;

    @ToString.Exclude
    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReaderBoardReplyLike> replyLikes = new ArrayList<>();

    @Builder
    public ReaderBoardReply(ReaderBoard board, Long userId, String comment) {
        this.board = board;
        this.userId = userId;
        this.comment = comment;
    }

    @Override
    public Long getBoardId() {
        return board.getId();
    }
}