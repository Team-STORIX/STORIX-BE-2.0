package com.storix.domain.domains.chat.adaptor;

import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.chat.dto.ChatMessageText;
import com.storix.domain.domains.chat.repository.ChatRepository;
import com.storix.domain.domains.topicroom.dto.RecentSenderRow;
import com.storix.domain.domains.topicroom.dto.RoomLastMessageId;
import com.storix.domain.domains.topicroom.dto.RoomUnreadCount;
import com.storix.domain.domains.topicroom.dto.UserUnreadCount;
import com.storix.domain.domains.user.dto.AdminUserContentItemResponse;
import com.storix.common.utils.STORIXStatic;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatAdaptor {

    private final PlatformTransactionManager transactionManager;
    private final ChatRepository chatRepository;

    public Slice<ChatMessageResponseDto> loadMessages(Long roomId, Pageable pageable) {
        return chatRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    public List<ChatMessageResponseDto> loadRecentMessagesBySender(Long roomId, Long senderId, Pageable pageable) {
        return chatRepository.findRecentByRoomIdAndSenderId(roomId, senderId, pageable);
    }

    public ChatMessageResponseDto findAdminMessageById(Long messageId) {
        return chatRepository.findAdminMessageById(messageId);
    }

    public Page<AdminUserContentItemResponse> findAdminChatContentsByUserId(Long userId, Pageable pageable) {
        return chatRepository.findAdminChatContentsByUserId(userId, pageable);
    }

    public List<AdminUserContentItemResponse> findAdminChatContentsByIds(List<Long> ids) {
        return chatRepository.findAdminChatContentsByIds(ids);
    }

    public long countActiveChatsByUserId(Long userId) {
        return chatRepository.countBySenderIdAndDeletedFalse(userId);
    }

    public ChatMessage saveMessage(ChatMessage message) {
        return chatRepository.save(message);
    }

    public int softDeleteTalkMessagesBySender(Long roomId, Long senderId) {
        return chatRepository.softDeleteByRoomIdAndSenderId(roomId, senderId, MessageType.TALK, LocalDateTime.now());
    }

    public int softDeleteTalkMessageBySender(Long messageId, Long senderId) {
        return chatRepository.softDeleteByIdAndSenderId(messageId, senderId, MessageType.TALK, LocalDateTime.now());
    }

    // 청크 단위 트랜잭션으로 삭제
    public int hardDeleteBefore(LocalDateTime cutoff) {
        TransactionTemplate chunkTx = new TransactionTemplate(transactionManager);
        chunkTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        int total = 0;
        while (true) {
            Integer deleted = chunkTx.execute(status -> {
                List<Long> ids = chatRepository.findIdsForHardDelete(
                        cutoff, PageRequest.of(0, STORIXStatic.HARD_DELETE_CHUNK_SIZE));
                return ids.isEmpty() ? 0 : chatRepository.hardDeleteByIds(ids);
            });
            if (deleted == null || deleted == 0) return total;
            total += deleted;
        }
    }

    public Slice<ChatMessageResponseDto> loadMessages(Long roomId, List<Long> blockedIds, Pageable pageable) {
        if (blockedIds.isEmpty()) {
            return chatRepository.findAllByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        }
        return chatRepository.findAllByRoomIdExcludingBlockedOrderByCreatedAtDesc(roomId, blockedIds, pageable);
    }

    public Map<Long, Integer> findUnreadCountMap(Long userId, List<Long> roomIds) {
        if (roomIds.isEmpty()) return Map.of();
        return countUnreadByRoomIds(userId, roomIds).stream()
                .collect(Collectors.toMap(RoomUnreadCount::roomId, r -> r.unreadCount().intValue()));
    }

    public List<RoomUnreadCount> countUnreadByRoomIds(Long userId, List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return List.of();
        }
        return chatRepository.countUnreadByRoomIds(userId, roomIds, MessageType.TALK);
    }

    public long countTotalUnread(Long userId) {
        return chatRepository.countTotalUnreadByUserId(userId, MessageType.TALK);
    }

    public boolean existsUnread(Long userId) {
        return chatRepository.existsUnreadByUserId(userId, MessageType.TALK);
    }

    public Long findLastMessageId(Long roomId) {
        return chatRepository.findLastMessageIdByRoomId(roomId);
    }

    public List<RoomLastMessageId> findLastMessageIdsByRoomIds(List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return List.of();
        }
        return chatRepository.findLastMessageIdsByRoomIds(roomIds, MessageType.TALK);
    }

    public ChatMessageResponseDto findLatestMessage(Long roomId) {
        return chatRepository.findLatestByRoomId(roomId, MessageType.TALK, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<UserUnreadCount> countMessagesAfterForUsers(
            Long roomId, List<Long> userIds, Long afterMessageId, Long upToMessageId) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return chatRepository.countMessagesAfterForUsers(
                roomId, userIds, afterMessageId, upToMessageId, MessageType.TALK);
    }

    public List<RecentSenderRow> findRecentSenderRows(
            Long roomId, List<Long> userIds, Long afterMessageId, Long upToMessageId) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return chatRepository.findRecentSenderRows(
                roomId, userIds, afterMessageId, upToMessageId, MessageType.TALK);
    }

    public List<ChatMessageText> findMessageTextsByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return chatRepository.findMessageTextsByIds(ids);
    }

    public List<UserUnreadCount> countTotalUnreadByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        return chatRepository.countTotalUnreadByUserIds(userIds, MessageType.TALK);
    }
}
