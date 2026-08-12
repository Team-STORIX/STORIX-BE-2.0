package com.storix.domain.domains.chat.repository;

import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.topicroom.dto.RecentSenderRow;
import com.storix.domain.domains.topicroom.dto.RoomLastMessageId;
import com.storix.domain.domains.topicroom.dto.RoomUnreadCount;
import com.storix.domain.domains.topicroom.dto.UserUnreadCount;
import com.storix.domain.domains.user.dto.AdminUserContentItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT new com.storix.domain.domains.chat.dto.ChatMessageResponseDto(" +
            "   m.id, " +
            "   m.roomId, " +
            "   m.senderId, " +
            "   COALESCE(" + com.storix.common.utils.STORIXStatic.NICK_NAME_DISPLAY_CASE_WHEN + ", '알 수 없음'), " +
            "   m.message, " +
            "   m.messageType, " +
            "   m.createdAt " +
            ") " +
            "FROM ChatMessage m " +
            "LEFT JOIN User u ON m.senderId = u.id " +
            "WHERE m.roomId = :roomId AND m.deleted = false " +
            "ORDER BY m.createdAt DESC, m.id DESC")
    Slice<ChatMessageResponseDto> findAllByRoomIdOrderByCreatedAtDesc(
            @Param("roomId") Long roomId,
            Pageable pageable
    );

    // 관리자 신고 상세용 — soft delete된 메시지도 포함해 원문 보존 (deleted 필터 의도적으로 제외)
    @Query("SELECT new com.storix.domain.domains.chat.dto.ChatMessageResponseDto(" +
            "   m.id, " +
            "   m.roomId, " +
            "   m.senderId, " +
            "   COALESCE(" + com.storix.common.utils.STORIXStatic.NICK_NAME_DISPLAY_CASE_WHEN + ", '알 수 없음'), " +
            "   m.message, " +
            "   m.messageType, " +
            "   m.createdAt " +
            ") " +
            "FROM ChatMessage m " +
            "LEFT JOIN User u ON m.senderId = u.id " +
            "WHERE m.roomId = :roomId AND m.senderId = :senderId " +
            "ORDER BY m.createdAt DESC, m.id DESC")
    List<ChatMessageResponseDto> findRecentByRoomIdAndSenderId(
            @Param("roomId") Long roomId,
            @Param("senderId") Long senderId,
            Pageable pageable
    );

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
            "WHERE m.id = :messageId")
    ChatMessageResponseDto findAdminMessageById(@Param("messageId") Long messageId);

    @Query("""
            SELECT new com.storix.domain.domains.user.dto.AdminUserContentItemResponse(
                m.id,
                com.storix.domain.domains.report.domain.TargetContentType.CHAT,
                null,
                null,
                m.roomId,
                null,
                m.message,
                null,
                m.messageType,
                0,
                0,
                m.createdAt
            )
            FROM ChatMessage m
            WHERE m.senderId = :userId AND m.deleted = false
            """)
    Page<AdminUserContentItemResponse> findAdminChatContentsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT new com.storix.domain.domains.user.dto.AdminUserContentItemResponse(
                m.id,
                com.storix.domain.domains.report.domain.TargetContentType.CHAT,
                null,
                null,
                m.roomId,
                null,
                m.message,
                null,
                m.messageType,
                0,
                0,
                m.createdAt
            )
            FROM ChatMessage m
            WHERE m.id IN :ids AND m.deleted = false
            """)
    List<AdminUserContentItemResponse> findAdminChatContentsByIds(@Param("ids") List<Long> ids);

    long countBySenderIdAndDeletedFalse(Long senderId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.deleted = true, m.deletedAt = :now " +
            "WHERE m.roomId = :roomId AND m.senderId = :senderId " +
            "AND m.messageType = :messageType AND m.deleted = false")
    int softDeleteByRoomIdAndSenderId(
            @Param("roomId") Long roomId,
            @Param("senderId") Long senderId,
            @Param("messageType") MessageType messageType,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE ChatMessage m SET m.deleted = true, m.deletedAt = :now " +
            "WHERE m.id = :messageId AND m.senderId = :senderId " +
            "AND m.messageType = :messageType AND m.deleted = false")
    int softDeleteByIdAndSenderId(
            @Param("messageId") Long messageId,
            @Param("senderId") Long senderId,
            @Param("messageType") MessageType messageType,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ChatMessage m WHERE m.deleted = true AND m.deletedAt < :cutoff")
    int hardDeleteBefore(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT new com.storix.domain.domains.chat.dto.ChatMessageResponseDto(" +
            "   m.id, " +
            "   m.roomId, " +
            "   m.senderId, " +
            "   COALESCE(" + com.storix.common.utils.STORIXStatic.NICK_NAME_DISPLAY_CASE_WHEN + ", '알 수 없음'), " +
            "   m.message, " +
            "   m.messageType, " +
            "   m.createdAt " +
            ") " +
            "FROM ChatMessage m " +
            "LEFT JOIN User u ON m.senderId = u.id " +
            "WHERE m.roomId = :roomId " +
            "AND m.senderId NOT IN :blockedIds AND m.deleted = false " +
            "ORDER BY m.createdAt DESC, m.id DESC")
    Slice<ChatMessageResponseDto> findAllByRoomIdExcludingBlockedOrderByCreatedAtDesc(
            @Param("roomId") Long roomId,
            @Param("blockedIds") List<Long> blockedIds,
            Pageable pageable
    );

    @Query("""
            SELECT new com.storix.domain.domains.topicroom.dto.RoomUnreadCount(m.roomId, COUNT(m.id))
            FROM ChatMessage m
            JOIN TopicRoomUser tu ON tu.topicRoom.id = m.roomId AND tu.userId = :userId
            WHERE m.roomId IN :roomIds
              AND m.deleted = false
              AND m.messageType = :messageType
              AND m.senderId <> :userId
              AND NOT EXISTS (
                  SELECT 1 FROM UserBlock b
                  WHERE b.blockerId = tu.userId AND b.blockedUserId = m.senderId
              )
              AND ((tu.lastReadMessageId IS NOT NULL AND m.id > tu.lastReadMessageId)
                OR (tu.lastReadMessageId IS NULL AND m.createdAt > tu.createdAt))
            GROUP BY m.roomId
            """)
    List<RoomUnreadCount> countUnreadByRoomIds(
            @Param("userId") Long userId,
            @Param("roomIds") List<Long> roomIds,
            @Param("messageType") MessageType messageType
    );

    @Query("""
            SELECT COUNT(m.id)
            FROM ChatMessage m
            JOIN TopicRoomUser tu ON tu.topicRoom.id = m.roomId AND tu.userId = :userId
            WHERE m.deleted = false
              AND m.messageType = :messageType
              AND m.senderId <> :userId
              AND NOT EXISTS (
                  SELECT 1 FROM UserBlock b
                  WHERE b.blockerId = tu.userId AND b.blockedUserId = m.senderId
              )
              AND ((tu.lastReadMessageId IS NOT NULL AND m.id > tu.lastReadMessageId)
                OR (tu.lastReadMessageId IS NULL AND m.createdAt > tu.createdAt))
            """)
    long countTotalUnreadByUserId(
            @Param("userId") Long userId,
            @Param("messageType") MessageType messageType
    );

    @Query("""
            SELECT COUNT(m.id) > 0
            FROM ChatMessage m
            JOIN TopicRoomUser tu ON tu.topicRoom.id = m.roomId AND tu.userId = :userId
            WHERE m.deleted = false
              AND m.messageType = :messageType
              AND m.senderId <> :userId
              AND NOT EXISTS (
                  SELECT 1 FROM UserBlock b
                  WHERE b.blockerId = tu.userId AND b.blockedUserId = m.senderId
              )
              AND ((tu.lastReadMessageId IS NOT NULL AND m.id > tu.lastReadMessageId)
                OR (tu.lastReadMessageId IS NULL AND m.createdAt > tu.createdAt))
            """)
    boolean existsUnreadByUserId(
            @Param("userId") Long userId,
            @Param("messageType") MessageType messageType
    );

    // 차단 여부가 유저마다 달라 NOT EXISTS 로 각자 기준을 적용
    @Query("""
            SELECT new com.storix.domain.domains.topicroom.dto.UserUnreadCount(tu.userId, COUNT(m.id))
            FROM ChatMessage m
            JOIN TopicRoomUser tu ON tu.topicRoom.id = m.roomId
            WHERE m.roomId = :roomId
              AND tu.userId IN :userIds
              AND m.id > :afterMessageId
              AND m.id <= :upToMessageId
              AND m.deleted = false
              AND m.messageType = :messageType
              AND m.senderId <> tu.userId
              AND NOT EXISTS (
                  SELECT 1 FROM UserBlock b
                  WHERE b.blockerId = tu.userId AND b.blockedUserId = m.senderId
              )
              AND ((tu.lastReadMessageId IS NOT NULL AND m.id > tu.lastReadMessageId)
                OR (tu.lastReadMessageId IS NULL AND m.createdAt > tu.createdAt))
            GROUP BY tu.userId
            """)
    List<UserUnreadCount> countMessagesAfterForUsers(
            @Param("roomId") Long roomId,
            @Param("userIds") List<Long> userIds,
            @Param("afterMessageId") Long afterMessageId,
            @Param("upToMessageId") Long upToMessageId,
            @Param("messageType") MessageType messageType
    );

    @Query("""
            SELECT new com.storix.domain.domains.topicroom.dto.RecentSenderRow(tu.userId, m.senderId, MAX(m.id))
            FROM ChatMessage m
            JOIN TopicRoomUser tu ON tu.topicRoom.id = m.roomId
            WHERE m.roomId = :roomId
              AND tu.userId IN :userIds
              AND m.id > :afterMessageId
              AND m.id <= :upToMessageId
              AND m.deleted = false
              AND m.messageType = :messageType
              AND m.senderId <> tu.userId
              AND NOT EXISTS (
                  SELECT 1 FROM UserBlock b
                  WHERE b.blockerId = tu.userId AND b.blockedUserId = m.senderId
              )
              AND ((tu.lastReadMessageId IS NOT NULL AND m.id > tu.lastReadMessageId)
                OR (tu.lastReadMessageId IS NULL AND m.createdAt > tu.createdAt))
            GROUP BY tu.userId, m.senderId
            """)
    List<RecentSenderRow> findRecentSenderRows(
            @Param("roomId") Long roomId,
            @Param("userIds") List<Long> userIds,
            @Param("afterMessageId") Long afterMessageId,
            @Param("upToMessageId") Long upToMessageId,
            @Param("messageType") MessageType messageType
    );

    @Query("""
            SELECT new com.storix.domain.domains.topicroom.dto.UserUnreadCount(tu.userId, COUNT(m.id))
            FROM ChatMessage m
            JOIN TopicRoomUser tu ON tu.topicRoom.id = m.roomId
            WHERE tu.userId IN :userIds
              AND m.deleted = false
              AND m.messageType = :messageType
              AND m.senderId <> tu.userId
              AND NOT EXISTS (
                  SELECT 1 FROM UserBlock b
                  WHERE b.blockerId = tu.userId AND b.blockedUserId = m.senderId
              )
              AND ((tu.lastReadMessageId IS NOT NULL AND m.id > tu.lastReadMessageId)
                OR (tu.lastReadMessageId IS NULL AND m.createdAt > tu.createdAt))
            GROUP BY tu.userId
            """)
    List<UserUnreadCount> countTotalUnreadByUserIds(
            @Param("userIds") List<Long> userIds,
            @Param("messageType") MessageType messageType
    );

    @Query("""
            SELECT new com.storix.domain.domains.topicroom.dto.RoomLastMessageId(m.roomId, MAX(m.id))
            FROM ChatMessage m
            WHERE m.roomId IN :roomIds
              AND m.deleted = false
              AND m.messageType = :messageType
            GROUP BY m.roomId
            """)
    List<RoomLastMessageId> findLastMessageIdsByRoomIds(
            @Param("roomIds") List<Long> roomIds,
            @Param("messageType") MessageType messageType
    );

    @Query("SELECT new com.storix.domain.domains.chat.dto.ChatMessageResponseDto(" +
            "   m.id, m.roomId, m.senderId, " +
            "   COALESCE(" + com.storix.common.utils.STORIXStatic.NICK_NAME_DISPLAY_CASE_WHEN + ", '알 수 없음'), " +
            "   m.message, m.messageType, m.createdAt) " +
            "FROM ChatMessage m " +
            "LEFT JOIN User u ON m.senderId = u.id " +
            "WHERE m.roomId = :roomId AND m.deleted = false AND m.messageType = :messageType " +
            "ORDER BY m.id DESC")
    List<ChatMessageResponseDto> findLatestByRoomId(
            @Param("roomId") Long roomId,
            @Param("messageType") MessageType messageType,
            Pageable pageable
    );

    @Query("""
            SELECT MAX(m.id)
            FROM ChatMessage m
            WHERE m.roomId = :roomId AND m.deleted = false
            """)
    Long findLastMessageIdByRoomId(@Param("roomId") Long roomId);
}
