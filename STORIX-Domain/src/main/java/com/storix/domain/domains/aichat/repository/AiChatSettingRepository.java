package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChatSetting;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiChatSettingRepository extends JpaRepository<AiChatSetting, Long> {

    Optional<AiChatSetting> findByUserId(Long userId);

    // 기본값(19금 끔 · 상관없음 · 온보딩 안 봄)으로 없으면 만든다. user_id 유니크라 동시 요청도 한 행
    @Modifying
    @Query(value = """
            INSERT INTO ai_chat_settings (user_id, adult_enabled, works_types, created_at, updated_at)
            VALUES (:userId, false, '', NOW(6), NOW(6))
            ON DUPLICATE KEY UPDATE user_id = user_id
            """, nativeQuery = true)
    void insertIfAbsent(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AiChatSetting s where s.userId = :userId")
    Optional<AiChatSetting> findByUserIdForUpdate(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from AiChatSetting s where s.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
