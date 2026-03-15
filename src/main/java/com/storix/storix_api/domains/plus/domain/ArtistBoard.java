package com.storix.storix_api.domains.plus.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "artist_board",
        indexes = {
                @Index(name = "idx_artist_board_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtistBoard extends Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_board_id")
    private Long id;

    @Column(name = "is_content_for_fan")
    private boolean isContentForFan;

    @Column(name = "point")
    private Integer point;


    @Builder
    public ArtistBoard(Long userId, boolean isWorksSelected, Long worksId,
                       boolean isContentForFan, Integer point, String content) {
        this.userId = userId;
        this.isWorksSelected = isWorksSelected;
        this.worksId = worksId;
        this.isContentForFan = isContentForFan;
        this.point = point;
        this.content = content;
    }
}