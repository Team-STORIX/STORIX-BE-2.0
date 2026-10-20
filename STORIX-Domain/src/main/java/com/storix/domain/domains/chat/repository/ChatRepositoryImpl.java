package com.storix.domain.domains.chat.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.topicroom.dto.RoomUnreadCount;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.storix.domain.domains.chat.domain.QChatMessage.chatMessage;
import static com.storix.domain.domains.topicroom.domain.QTopicRoomUser.topicRoomUser;
import static com.storix.domain.domains.user.domain.QUserBlock.userBlock;

// 안 읽은 수는 방마다 (room_id = ? AND id > 읽은 위치) 상수 범위로 센다.
// 읽은 위치를 조인으로 가져와 비교하면 MySQL 이 범위 검색을 못 하고 방의 채팅을 전부 읽는다 (#402)
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RoomUnreadCount> countUnreadByRoomIds(Long userId, List<Long> roomIds, MessageType messageType) {
        List<Tuple> cursors = readCursors(userId, roomIds);
        if (cursors.isEmpty()) return List.of();

        return queryFactory
                .select(Projections.constructor(RoomUnreadCount.class, chatMessage.roomId, chatMessage.id.count()))
                .from(chatMessage)
                .where(unread(userId, cursors, messageType))
                .groupBy(chatMessage.roomId)
                .fetch();
    }

    @Override
    public long countTotalUnreadByUserId(Long userId, MessageType messageType) {
        List<Tuple> cursors = readCursors(userId, null);
        if (cursors.isEmpty()) return 0L;

        Long count = queryFactory
                .select(chatMessage.id.count())
                .from(chatMessage)
                .where(unread(userId, cursors, messageType))
                .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public boolean existsUnreadByUserId(Long userId, MessageType messageType) {
        List<Tuple> cursors = readCursors(userId, null);
        if (cursors.isEmpty()) return false;

        return queryFactory
                .selectOne()
                .from(chatMessage)
                .where(unread(userId, cursors, messageType))
                .fetchFirst() != null;
    }

    // 방마다 읽은 위치. roomIds 가 null 이면 참여한 방 전부
    private List<Tuple> readCursors(Long userId, List<Long> roomIds) {
        if (roomIds != null && roomIds.isEmpty()) return List.of();
        return queryFactory
                .select(topicRoomUser.topicRoom.id, topicRoomUser.lastReadMessageId)
                .from(topicRoomUser)
                .where(topicRoomUser.userId.eq(userId),
                        roomIds == null ? null : topicRoomUser.topicRoom.id.in(roomIds))
                .fetch();
    }

    private BooleanExpression unread(Long userId, List<Tuple> cursors, MessageType messageType) {
        BooleanBuilder afterCursor = new BooleanBuilder();
        for (Tuple cursor : cursors) {
            afterCursor.or(chatMessage.roomId.eq(cursor.get(topicRoomUser.topicRoom.id))
                    .and(chatMessage.id.gt(cursor.get(topicRoomUser.lastReadMessageId))));
        }
        return chatMessage.deleted.isFalse()
                .and(chatMessage.messageType.eq(messageType))
                .and(chatMessage.senderId.ne(userId))
                .and(JPAExpressions.selectOne()
                        .from(userBlock)
                        .where(userBlock.blockerId.eq(userId), userBlock.blockedUserId.eq(chatMessage.senderId))
                        .notExists())
                .and(afterCursor);
    }
}
