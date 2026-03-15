package com.storix.domain.domains.feed.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BoardLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "user_id", nullable = false)
    protected Long userId;

    public abstract Long getBoardId();
}