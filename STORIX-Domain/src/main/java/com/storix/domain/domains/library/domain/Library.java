package com.storix.domain.domains.library.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "libraries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Library {

    @Id @Column(name = "user_id")
    private Long id;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @Column(name = "board_count", nullable = false)
    private int boardCount = 0;


    /** 생성자 로직 **/
    @Builder
    public Library(Long userId) {
        this.id = userId;
    }

}
