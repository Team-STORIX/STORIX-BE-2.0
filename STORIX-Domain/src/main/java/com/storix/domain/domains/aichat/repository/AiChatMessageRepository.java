package com.storix.domain.domains.aichat.repository;

import com.storix.domain.domains.aichat.domain.AiChatMessage;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findByChatIdOrderByIdDesc(Long chatId, Limit limit);

    List<AiChatMessage> findByChatIdAndIdLessThanOrderByIdDesc(Long chatId, Long id, Limit limit);

    @Modifying(clearAutomatically = true)
    @Query("delete from AiChatMessage m where m.chatId = :chatId")
    int deleteAllByChatId(@Param("chatId") Long chatId);
}
