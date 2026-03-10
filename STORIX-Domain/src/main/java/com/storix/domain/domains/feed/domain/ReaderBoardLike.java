package com.storix.domain.domains.feed.domain;

import com.storix.domain.domains.plus.domain.ReaderBoard;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "reader_board_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reader_board_like",
                        columnNames = {"user_id", "reader_board_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderBoardLike extends BoardLike {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_board_id", nullable = false)
    private ReaderBoard board;

    @Builder
    public ReaderBoardLike(ReaderBoard board, Long userId) {
        this.board = board;
        this.userId = userId;
    }


    public static ReaderBoardLike of(ReaderBoard board, Long userId) {
        return new ReaderBoardLike(board, userId);
    }

    @Override
    public Long getBoardId() {
        return board.getId();
    }
}