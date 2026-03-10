package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.OAuthProvider;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.user.dto.FavoriteArtistInfo;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import com.storix.domain.domains.user.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByActiveNickName(String activeNickName);

    @Query("SELECT u.role FROM User u WHERE u.id = :userId ")
    Optional<Role> findRoleByUserId(@Param("userId") Long userId);

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

    Optional<User> findArtistUserByLoginId(String loginId);

    Optional<User> findByOauthInfoProviderAndOauthInfoOid(
            OAuthProvider provider,
            String oid
    );

    Slice<User> findByRoleAndNickNameContaining(Role role, String nickName, Pageable pageable);

    @Query("SELECT u.isAdultVerified FROM User u WHERE u.id = :userId")
    Boolean findIsAdultVerifiedById(@Param("userId") Long userId);

    // 관심 작가 관련 정보 조회
    @Query("SELECT new com.storix.domain.domains.user.dto.FavoriteArtistInfo(u.id, u.profileImageUrl, u.nickName, u.profileDescription) " +
            "FROM User u " +
            "WHERE u.id IN :artistIds ")
    List<FavoriteArtistInfo> findFavoriteArtistInfosByIds(@Param("artistIds") List<Long> artistIds);

    // 단건 프로필 정보 조회
    @Query("SELECT new com.storix.domain.domains.user.dto.StandardProfileInfo(u.id, u.profileImageUrl, u.nickName)" +
            "FROM User u " +
            "WHERE u.id = :userId ")
    StandardProfileInfo findStandardProfileInfoById(@Param("userId") Long userId);

    // 단체 프로필 정보 조회
    @Query("SELECT new com.storix.domain.domains.user.dto.StandardProfileInfo(u.id, u.profileImageUrl, u.nickName)" +
            "FROM User u " +
            "WHERE u.id IN :userIds ")
    List<StandardProfileInfo> findStandardProfileInfoByUserIds(@Param("userIds") List<Long> userIds);

}
