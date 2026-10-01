package com.storix.domain.domains.feed.domain;

import com.storix.domain.domains.plus.domain.ReaderBoard;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "reader_board_bookmark",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reader_board_bookmark",
                        columnNames = {"user_id", "reader_board_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderBoardBookmark {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_board_id", nullable = false)
    private ReaderBoard board;
}
