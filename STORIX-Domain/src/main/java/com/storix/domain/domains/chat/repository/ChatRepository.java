package com.storix.domain.domains.chat.repository;

import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT new com.storix.domain.domains.chat.dto.ChatMessageResponseDto(" +
            "   m.id, " +
            "   m.roomId, " +
            "   m.senderId, " +
            "   COALESCE(u.nickName, '알 수 없음'), " +
            "   m.message, " +
            "   m.messageType, " +
            "   m.createdAt " +
            ") " +
            "FROM ChatMessage m " +
            "LEFT JOIN User u ON m.senderId = u.id " +
            "WHERE m.roomId = :roomId " +
            "ORDER BY m.createdAt DESC, m.id DESC")
    Slice<ChatMessageResponseDto> findAllByRoomIdOrderByCreatedAtDesc(
            @Param("roomId") Long roomId,
            Pageable pageable
    );
}