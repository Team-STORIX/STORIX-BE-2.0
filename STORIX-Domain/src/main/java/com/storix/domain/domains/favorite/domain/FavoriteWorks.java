package com.storix.domain.domains.favorite.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        name="user_favorite_works",
        uniqueConstraints = @UniqueConstraint(
                name="uk_favorite_user_work",
                columnNames={"user_id","works_id"}
        ),
        indexes = {
                @Index(name="idx_favorite_user", columnList="user_id"),
                @Index(name="idx_favorite_work", columnList="works_id")
        }
)
@Getter @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteWorks {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_works_id")
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, name = "works_id")
    private Long worksId;

    @Builder
    public FavoriteWorks(Long userId, Long worksId) {
        this.userId = userId;
        this.worksId = worksId;
    }
}
