package com.storix.domain.domains.preference.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "taste_exploration",
        indexes = {
            @Index(name = "idx_user_liked", columnList = "user_id, is_liked")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_works", columnNames = {"user_id", "works_id"})
        }
)
public class PreferenceExploration extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "works_id", nullable = false)
    private Long worksId;

    @Column(name = "is_liked", nullable = false)
    private boolean isLiked;

    @Builder
    public PreferenceExploration(Long userId, Long worksId, boolean isLiked) {
        this.userId = userId;
        this.worksId = worksId;
        this.isLiked = isLiked;
    }
}
