package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiChatRepository extends JpaRepository<AiChat, Long> {

    Optional<AiChat> findByUserId(Long userId);

    // user_id 유니크 기반. 같은 사용자 동시 요청도 충돌 예외 없이 한 행만 남는다
    @Modifying
    @Query(value = """
            INSERT INTO ai_chats (user_id, created_at, updated_at)
            VALUES (:userId, NOW(6), NOW(6))
            ON DUPLICATE KEY UPDATE user_id = user_id
            """, nativeQuery = true)
    void insertIfAbsent(@Param("userId") Long userId);

    // 잠금 조회라 다른 트랜잭션이 막 커밋한 행도 본다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AiChat c where c.userId = :userId")
    Optional<AiChat> findByUserIdForUpdate(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from AiChat c where c.id = :chatId")
    int deleteByIdDirectly(@Param("chatId") Long chatId);
}
