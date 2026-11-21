package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiChatRepository extends JpaRepository<AiChat, Long> {

    Optional<AiChat> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("delete from AiChat c where c.id = :chatId")
    int deleteByIdDirectly(@Param("chatId") Long chatId);
}
