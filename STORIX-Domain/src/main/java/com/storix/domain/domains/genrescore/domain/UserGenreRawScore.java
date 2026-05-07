package com.storix.domain.domains.genrescore.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.works.domain.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_genre_raw_score",
        indexes = {
                @Index(name = "idx_user", columnList = "user_id")
        }
)
public class UserGenreRawScore extends BaseTimeEntity {

    @EmbeddedId
    private UserGenreRawScoreId id;

    @Column(name = "raw_score", nullable = false)
    private long rawScore;

    public Long getUserId() {
        return id.getUserId();
    }

    public Genre getGenre() {
        return id.getGenre();
    }
}
