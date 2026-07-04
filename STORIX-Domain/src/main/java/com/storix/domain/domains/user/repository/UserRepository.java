package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.user.dto.UserNicknameInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.storix.domain.domains.user.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByActiveNickName(String activeNickName);

    @Query("""
        SELECT (COUNT(u) > 0)
        FROM User u
        WHERE u.activeNickName = :nickName
          AND u.id <> :userId
          AND u.accountState = com.storix.domain.domains.user.domain.AccountState.NORMAL
    """)
    boolean existsNickNameExceptSelf(
            @Param("nickName") String nickName,
            @Param("userId") Long userId
    );

    Optional<User> findByOauthInfoProviderAndOauthInfoOid(
            OAuthProvider provider,
            String oid
    );

    // 관리자 이메일 검증
    Optional<User> findByOauthInfoEmail(String email);

    @Query("SELECT u.isAdultVerified FROM User u WHERE u.id = :userId")
    Boolean findIsAdultVerifiedById(@Param("userId") Long userId);

    // 단건 프로필 정보 조회
    @Query("SELECT new com.storix.domain.domains.user.dto.StandardProfileInfo(u.id, u.profileObjectKey, u.nickName)" +
            "FROM User u " +
            "WHERE u.id = :userId ")
    StandardProfileInfo findStandardProfileInfoById(@Param("userId") Long userId);

    // 단체 프로필 정보 조회
    @Query("SELECT new com.storix.domain.domains.user.dto.StandardProfileInfo(u.id, u.profileObjectKey, u.nickName)" +
            "FROM User u " +
            "WHERE u.id IN :userIds ")
    List<StandardProfileInfo> findStandardProfileInfoByUserIds(@Param("userIds") List<Long> userIds);

    // 정지 만료 유저 일괄 복구 — suspendedUntil 기준 (ReportCase 독립적)
    // restore()는 accountState/suspendedUntil 변경 외 부수효과가 없으므로 엔티티 로딩 없이 벌크 UPDATE로 처리
    @Modifying
    @Query("UPDATE User u SET u.accountState = com.storix.domain.domains.user.domain.AccountState.NORMAL, u.suspendedUntil = null " +
            "WHERE u.accountState = :state AND u.suspendedUntil < :now")
    int restoreExpiredSuspensions(
            @Param("state") AccountState state,
            @Param("now") LocalDateTime now);

    @Query("""
        SELECT new com.storix.domain.domains.user.dto.UserNicknameInfo(u.id, u.nickName)
        FROM User u
        WHERE u.id IN :userIds
    """)
    List<UserNicknameInfo> findNicknameInfoByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM User u WHERE u.accountState = com.storix.domain.domains.user.domain.AccountState.DELETED AND u.deletedAt < :cutoff")
    int hardDeleteBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.title = null")
    void clearAllTitles();

    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.title IS NULL
          AND EXISTS (
              SELECT s.id.userId
              FROM UserGenreRawScore s
              WHERE s.id.userId = u.id
          )
        ORDER BY u.id ASC
    """)
    List<Long> findUntitledUserIdsHavingRawScore(Pageable pageable);

    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.accountState = com.storix.domain.domains.user.domain.AccountState.NORMAL
          AND u.role <> com.storix.domain.domains.user.domain.Role.SUPER_ADMIN
          AND (:lastUserId IS NULL OR u.id > :lastUserId)
          AND (:signupCutoff IS NULL OR u.createdAt >= :signupCutoff)
        ORDER BY u.id ASC
    """)
    List<Long> findAdminNotificationTargetUserIds(
            @Param("lastUserId") Long lastUserId,
            @Param("signupCutoff") LocalDateTime signupCutoff,
            Pageable pageable
    );

}
