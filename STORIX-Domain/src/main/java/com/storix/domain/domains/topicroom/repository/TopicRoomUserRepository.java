package com.storix.domain.domains.topicroom.repository;

import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.dto.RoomMember;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TopicRoomUserRepository extends JpaRepository<TopicRoomUser, Long> {

    @Query("select tru from TopicRoomUser tru join fetch tru.topicRoom where tru.userId = :userId")
    Slice<TopicRoomUser> findByUserIdWithTopicRoom(@Param("userId") Long userId, Pageable pageable);

    long countByUserId(Long userId);

    boolean existsByUserIdAndTopicRoomId(Long userId, Long topicRoomId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TopicRoomUser tru WHERE tru.userId = :userId AND tru.topicRoom.id = :roomId")
    int deleteByUserIdAndTopicRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT tu.topicRoom.id FROM TopicRoomUser tu " +
            "WHERE tu.userId = :userId AND tu.topicRoom.id IN :roomIds")
    Set<Long> findJoinedRoomIdsByUserIdAndRoomIds(
            @Param("userId") Long userId,
            @Param("roomIds") Collection<Long> roomIds
    );

    @Query("SELECT tu.topicRoom.id FROM TopicRoomUser tu WHERE tu.userId = :userId")
    List<Long> findAllJoinedRoomIdsByUserId(@Param("userId") Long userId);

    // 특정 방의 userId 조회
    @Query("SELECT tu.userId " +
            "FROM TopicRoomUser tu " +
            "WHERE tu.topicRoom.id = :roomId")
    List<Long> findMemberIdsByRoomId(@Param("roomId") Long roomId);

    // 여러 방의 멤버를 한 번에 조회
    @Query("SELECT new com.storix.domain.domains.topicroom.dto.RoomMember(tu.topicRoom.id, tu.userId) " +
            "FROM TopicRoomUser tu " +
            "WHERE tu.topicRoom.id IN :roomIds")
    List<RoomMember> findMembersByRoomIds(@Param("roomIds") List<Long> roomIds);

    Optional<TopicRoomUser> findByUserIdAndTopicRoomId(Long userId, Long topicRoomId);
}