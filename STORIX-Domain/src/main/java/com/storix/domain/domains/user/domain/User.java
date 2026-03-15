package com.storix.domain.domains.user.domain;

import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.user.exception.auth.AlreadyWithDrawUserException;
import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_loginId", columnList = "loginId")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_oauth_provider_oid",
                        columnNames = {"oauth_provider", "oauth_oid"}
                ),
                @UniqueConstraint(
                        name = "uk_active_nick_name",
                        columnNames = "active_nick_name"
                )
        }
)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 계정 정보
    @Column(name = "nick_name", nullable = false, length = 10)
    private String nickName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(
            name = "user_favorite_genre",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private Set<Genre> favoriteGenreList = new HashSet<>();

    @Column(name = "profile_image_url")
    private String profileImageUrl = null;

    @Column(length = 30)
    private String profileDescription;

    @Column(nullable = false)
    private int level = 1;

    @Min(0)
    @Column(nullable = false)
    private int point = 0;

    @Column(name = "market_agree")
    private Boolean marketingAgree;

    @Column(name = "is_adult_verified")
    private Boolean isAdultVerified = false;

    // 계정 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "account_state", nullable = false)
    private AccountState accountState = AccountState.NORMAL;

    @Column(name = "deletedSuffix", length = 36)
    private String deletedSuffix;

    @Column(name = "active_nick_name", insertable = false, updatable = false, length = 50)
    private String activeNickName;

    // 계정 권한
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.READER;

    // 독자용 소셜 로그인
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "provider", column = @Column(name = "oauth_provider")),
            @AttributeOverride(name = "oid", column = @Column(name = "oauth_oid"))
    })
    private OAuthInfo oauthInfo;

    /** 생성자 로직 **/
    protected User() {}

    @Builder(builderMethodName = "readerBuilder")
    public User(boolean marketingAgree, OAuthInfo oauthInfo, String nickName, Gender gender, Set<Genre> favoriteGenreList) {
        this.marketingAgree = marketingAgree;
        this.oauthInfo = oauthInfo;
        this.nickName = nickName;
        this.gender = gender;
        this.favoriteGenreList = favoriteGenreList;
    }

    /** 비즈니스 로직 **/
    public void login() {
        // 계정 상태 = 정지 -> 로그인 제한 Exception
        lastLoginAt = LocalDateTime.now();
    }

    // 계정 정보 수정
    public void changeNickName(String nickName) {
        this.nickName = nickName;
    }

    public void changeProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public void changeProfileImage(String objectKey) { this.profileImageUrl = objectKey; }

    public void changeLevel(int level) {
//        if (level < 1 || level > 5) {
//            throw new IllegalArgumentException("레벨 범위 오류");
//        }
        this.level = level;
    }

    public void increasePoint(int point) {
        this.point += point;
    }

    public void decreasePoint(int point) {
//        if (this.point < point) {
//            throw new IllegalArgumentException("포인트 부족"); -> 커스텀 에러
//        }
        this.point -= point;
    }

    // 계정 탈퇴
    public void withdraw() {
        if (accountState.equals(AccountState.DELETED)) {
            throw AlreadyWithDrawUserException.EXCEPTION;
        }
        accountState = AccountState.DELETED;
        deletedSuffix = UUID.randomUUID().toString();
        gender = null;
        favoriteGenreList = null;
        profileImageUrl = null;
        nickName = "탈퇴한 유저";
        oauthInfo = oauthInfo.withDrawOauthInfo();
        marketingAgree = null;
        isAdultVerified = null;
    }

}
