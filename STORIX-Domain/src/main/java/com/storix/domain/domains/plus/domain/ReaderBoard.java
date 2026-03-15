package com.storix.domain.domains.plus.domain;

import com.storix.domain.domains.feed.domain.ReaderBoardLike;
import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "reader_board",
        indexes = {
                @Index(name = "idx_reader_board_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReaderBoard extends Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reader_board_id")
    private Long id;

    @Column(name = "is_spoiler")
    private boolean isSpoiler;

    @Column(name = "popularity_score")
    private int popularityScore = 0;

    @ToString.Exclude
    @OneToMany(mappedBy = "readerBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReaderBoardImage> images = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReaderBoardReply> replies = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReaderBoardLike> likes = new ArrayList<>();


    @Builder
    public ReaderBoard(Long userId, boolean isWorksSelected,
                       Long worksId, boolean isSpoiler, String content) {
        this.userId = userId;
        this.isWorksSelected = isWorksSelected;
        this.worksId = worksId;
        this.isSpoiler = isSpoiler;
        this.content = content;
    }
}