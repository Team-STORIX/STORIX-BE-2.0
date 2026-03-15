package com.storix.storix_api.domains.review.domain;

import com.storix.storix_api.domains.plus.domain.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "review_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_like",
                        columnNames = {"user_id", "review_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "user_id", nullable = false)
    protected Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Builder
    public ReviewLike(Long userId, Review review) {
        this.userId = userId;
        this.review = review;
    }

    public static ReviewLike of(Long userId, Review review) {
        return new ReviewLike(userId, review);
    }

}
