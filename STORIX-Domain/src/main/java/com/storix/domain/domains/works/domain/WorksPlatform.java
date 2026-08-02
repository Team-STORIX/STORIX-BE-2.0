package com.storix.domain.domains.works.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "works_platform",
        uniqueConstraints = @UniqueConstraint(columnNames = {"works_id", "platform"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorksPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "works_id", nullable = false)
    private Works works;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "landing_url", length = 500)
    private String landingUrl;

    public WorksPlatform(Works works, Platform platform) {
        this(works, platform, null);
    }

    public WorksPlatform(Works works, Platform platform, String landingUrl) {
        this.works = works;
        this.platform = platform;
        this.landingUrl = landingUrl;
    }
}
