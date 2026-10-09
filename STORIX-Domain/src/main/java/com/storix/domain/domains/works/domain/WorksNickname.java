package com.storix.domain.domains.works.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.search.helper.HangulTextHelper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "works_nickname",
        uniqueConstraints = @UniqueConstraint(name = "uk_works_nickname", columnNames = {"works_id", "normalized"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorksNickname extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "works_nickname_id")
    private Long id;

    @Column(name = "works_id", nullable = false)
    private Long worksId;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String normalized;

    public WorksNickname(Long worksId, String nickname) {
        this.worksId = worksId;
        this.nickname = nickname;
        this.normalized = HangulTextHelper.normalize(nickname);
    }
}
