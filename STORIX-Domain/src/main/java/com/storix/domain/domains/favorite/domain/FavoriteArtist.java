package com.storix.domain.domains.favorite.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        name="user_favorite_artist",
        uniqueConstraints = @UniqueConstraint(
                name="uk_favorite_user_artist",
                columnNames={"user_id","artist_id"}
        ),
        indexes = {
                @Index(name="idx_favorite_user", columnList="user_id"),
                @Index(name="idx_favorite_artist", columnList="artist_id")
        }
)
@Getter @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteArtist {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_artist_id")
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, name = "artist_id")
    private Long artistId;

    @Builder
    public FavoriteArtist(Long userId, Long artistId) {
        this.userId = userId;
        this.artistId = artistId;
    }
}
