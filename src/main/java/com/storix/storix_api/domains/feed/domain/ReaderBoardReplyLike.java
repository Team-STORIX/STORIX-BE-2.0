package com.storix.storix_api.domains.feed.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "reader_board_reply_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reader_board_reply_like",
                        columnNames = {"user_id", "reader_board_reply_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderBoardReplyLike extends BoardLike {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_board_reply_id", nullable = false)
    private ReaderBoardReply reply;

    @Builder
    public ReaderBoardReplyLike(ReaderBoardReply reply, Long userId) {
        this.reply = reply;
        this.userId = userId;
    }

    public static ReaderBoardReplyLike of(ReaderBoardReply reply, Long userId) {
        return new ReaderBoardReplyLike(reply, userId);
    }

    @Override
    public Long getBoardId() {
        return reply.getId();
    } // 댓글 id
}