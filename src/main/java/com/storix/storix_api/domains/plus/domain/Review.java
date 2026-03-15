package com.storix.storix_api.domains.plus.domain;

import com.storix.storix_api.domains.plus.adaptor.RatingConverter;
import com.storix.storix_api.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_library_works",
                        columnNames = {"library_user_id", "works_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // 서재 도메인
    @Column(name = "library_user_id", nullable = false)
    private Long libraryUserId;

    // 리뷰 작품 정보
    @Column(name = "works_id", nullable = false)
    private Long worksId;

    // 리뷰 내용 정보
    @Column(name = "is_spoiler")
    private boolean isSpoiler;

    @Column(precision = 2, scale = 1, nullable = false)
    @Convert(converter = RatingConverter.class)
    private Rating rating;

    @Column(length = 500, nullable = false)
    private String content;

    private int likeCount = 0;

    /** 생성자 로직 **/
    @Builder
    public Review (Long libraryUserId, Long worksId, boolean isSpoiler, Rating rating, String content) {
        this.libraryUserId = libraryUserId;
        this.worksId = worksId;
        this.isSpoiler = isSpoiler;
        this.rating = rating;
        this.content = content;
    }

}
