package com.storix.domain.domains.user.domain;

import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.user.exception.auth.AlreadyWithDrawUserException;
import com.storix.domain.domains.user.exception.auth.SuspendedUserException;
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

    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(
            name = "user_favorite_genre",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private Set<Genre> favoriteGenreList = new HashSet<>();

    @Column(name = "profile_object_key")
    private String profileObjectKey = null;

    @Column(length = 30)
    private String profileDescription;

    // 칭호
    @Enumerated(EnumType.STRING)
    @Column(name = "title", length = 40)
    private Title title;

    @Min(0)
    @Column(nullable = false)
    private int point = 0;

    // 만 14세 이상 동의 여부
    @Column(name = "age_over_14")
    private Boolean ageOver14;

    @Column(name = "is_adult_verified")
    private Boolean isAdultVerified = false;

    // 계정 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "account_state", nullable = false)
    private AccountState accountState = AccountState.NORMAL;

    @Column(name = "deletedSuffix", length = 36)
    private String deletedSuffix;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "active_nick_name", insertable = false, updatable = false, length = 50)
    private String activeNickName;

    // 계정 권한
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    // 관리자 비밀번호
    @Column(name = "password", length = 100)
    private String password;

    // 소셜 로그인
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "provider", column = @Column(name = "oauth_provider")),
            @AttributeOverride(name = "oid", column = @Column(name = "oauth_oid")),
            @AttributeOverride(name = "oauthRefreshToken", column = @Column(name = "oauth_refresh_token", length = 1024)),
            @AttributeOverride(name = "email", column = @Column(name = "oauth_email"))
    })
    private OAuthInfo oauthInfo;

    /** 생성자 로직 **/
    protected User() {}

    @Builder
    public User(Boolean ageOver14, OAuthInfo oauthInfo, String nickName, Set<Genre> favoriteGenreList, String profileDescription, Role role, String password) {
        this.ageOver14 = ageOver14;
        this.oauthInfo = oauthInfo;
        this.nickName = nickName;
        this.favoriteGenreList = favoriteGenreList;
        this.profileDescription = profileDescription;
        if (role != null) this.role = role;
        this.password = password;
    }

    /** 비즈니스 로직 **/
    public void login() {
        checkActiveOrThrow();
        lastLoginAt = LocalDateTime.now();
    }

    // 계정 상태 검증 - 탈퇴 계정은 차단, 정지 기간이 만료된 계정은 자동 해제 후 통과
    public void checkActiveOrThrow() {
        if (accountState == AccountState.DELETED) {
            throw AlreadyWithDrawUserException.EXCEPTION;
        }
        if (accountState == AccountState.SUSPENDED) {
            if (suspendedUntil != null && suspendedUntil.isBefore(LocalDateTime.now())) {
                restore();
                return;
            }
            throw SuspendedUserException.EXCEPTION;
        }
    }

    // refresh_token 갱신 (X 등 회전형)
    public void updateOauthRefreshToken(String oauthRefreshToken) {
        if (oauthRefreshToken != null) {
            this.oauthInfo.updateOauthRefreshToken(oauthRefreshToken);
        }
    }

    // 계정 정보 수정
    public void changeNickName(String nickName) {
        this.nickName = nickName;
    }

    public void changeProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public void changeProfileImage(String objectKey) { this.profileObjectKey = objectKey; }

    public void changeTitle(Title title) {
        this.title = title;
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

    // 계정 정지 (기간 지정)
    public void suspend(LocalDateTime until) {
        if (accountState == AccountState.DELETED) {
            throw AlreadyWithDrawUserException.EXCEPTION;
        }
        if (until == null || !until.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("정지 만료 시각은 현재 이후여야 합니다");
        }
        this.accountState = AccountState.SUSPENDED;
        this.suspendedUntil = until;
    }

    // 계정 정지 해제
    public void restore() {
        this.accountState = AccountState.NORMAL;
        this.suspendedUntil = null;
    }

    // 계정 탈퇴
    public void withdraw() {
        if (accountState.equals(AccountState.DELETED)) {
            throw AlreadyWithDrawUserException.EXCEPTION;
        }
        accountState = AccountState.DELETED;
        deletedSuffix = UUID.randomUUID().toString();
        favoriteGenreList = null;
        profileObjectKey = null;
        nickName = "탈퇴한 유저";
        oauthInfo = oauthInfo.withDrawOauthInfo();
        ageOver14 = null;
        isAdultVerified = null;
        deletedAt = LocalDateTime.now();
    }

}
