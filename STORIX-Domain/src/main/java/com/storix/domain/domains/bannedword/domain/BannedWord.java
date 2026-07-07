package com.storix.domain.domains.bannedword.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "banned_words",
        uniqueConstraints = @UniqueConstraint(name = "uk_banned_word", columnNames = "word")
)
public class BannedWord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banned_word_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String word;

    @Builder
    public BannedWord(String word) {
        this.word = word;
    }
}
