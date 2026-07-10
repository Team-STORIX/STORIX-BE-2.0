package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.dto.AdminUserListResponse;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.user.dto.UserNicknameInfo;
import org.springframework.data.domain.Page;
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

    boolean existsByNickName(String nickName);

    @Query("""
        SELECT (COUNT(u) > 0)
        FROM User u
        WHERE u.nickName = :nickName
          AND u.id <> :userId
    """)
    boolean existsNickNameExceptSelf(
            @Param("nickName") String nickName,
            @Param("userId") Long userId
    );

    Optional<User> findByOauthInfoProviderAndOauthInfoOid(
            OAuthProvider provider,
            String oid
    );

    @Query("""
        SELECT new com.storix.domain.domains.user.dto.AdminUserListResponse(
            u.id,
            u.nickName,
            u.oauthInfo.email,
            u.oauthInfo.provider,
            u.createdAt,
            u.accountState,
            u.suspendedUntil,
            u.lastLoginAt
        )
        FROM User u
        WHERE (:userId IS NULL OR u.id = :userId)
          AND (:nickName IS NULL OR u.nickName LIKE CONCAT('%', :nickName, '%'))
          AND (:accountState IS NULL OR u.accountState = :accountState)
          AND u.role = com.storix.domain.domains.user.domain.Role.READER
    """)
    Page<AdminUserListResponse> searchAdminUsers(
            @Param("userId") Long userId,
            @Param("nickName") String nickName,
            @Param("accountState") AccountState accountState,
            Pageable pageable
    );

    // 관리자 이메일 검증
    Optional<User> findByOauthInfoEmail(String email);

    @Query("SELECT u.isAdultVerified FROM User u WHERE u.id = :userId")
    Boolean findIsAdultVerifiedById(@Param("userId") Long userId);

    // 단건 프로필 정보 조회
    @Query("SELECT new com.storix.domain.domains.user.dto.StandardProfileInfo(" +
            "   u.id, u.profileObjectKey, " +
            "   " + com.storix.common.utils.STORIXStatic.NICK_NAME_DISPLAY_CASE_WHEN +
            ") " +
            "FROM User u " +
            "WHERE u.id = :userId ")
    StandardProfileInfo findStandardProfileInfoById(@Param("userId") Long userId);

    // 단체 프로필 정보 조회
    @Query("SELECT new com.storix.domain.domains.user.dto.StandardProfileInfo(" +
            "   u.id, u.profileObjectKey, " +
            "   " + com.storix.common.utils.STORIXStatic.NICK_NAME_DISPLAY_CASE_WHEN +
            ") " +
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

    @Query("SELECT new com.storix.domain.domains.user.dto.UserNicknameInfo(" +
            "   u.id, " +
            "   " + com.storix.common.utils.STORIXStatic.NICK_NAME_DISPLAY_CASE_WHEN +
            ") " +
            "FROM User u " +
            "WHERE u.id IN :userIds")
    List<UserNicknameInfo> findNicknameInfoByUserIds(@Param("userIds") List<Long> userIds);

    // 탈퇴 1년 경과 유저의 소셜 식별자 파기
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.oauthInfo.oid = null " +
            "WHERE u.accountState = com.storix.domain.domains.user.domain.AccountState.DELETED " +
            "AND u.deletedAt < :cutoff AND u.oauthInfo.oid IS NOT NULL")
    int purgeOauthOidBefore(@Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.title = null")
    void clearAllTitles();

    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.accountState = com.storix.domain.domains.user.domain.AccountState.NORMAL
          AND u.role <> com.storix.domain.domains.user.domain.Role.ADMIN
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
