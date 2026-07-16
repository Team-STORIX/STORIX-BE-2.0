package com.storix.domain.domains.user.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "user_block",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_block_blocker_blocked",
                        columnNames = {"blocker_id", "blocked_user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBlock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id", nullable = false)
    private Long blockerId;

    @Column(name = "blocked_user_id", nullable = false)
    private Long blockedUserId;

    public UserBlock(Long blockerId, Long blockedUserId) {
        this.blockerId = blockerId;
        this.blockedUserId = blockedUserId;
    }
}
