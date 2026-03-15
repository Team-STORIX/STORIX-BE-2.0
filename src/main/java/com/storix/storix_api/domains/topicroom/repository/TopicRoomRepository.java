package com.storix.storix_api.domains.topicroom.repository;

import com.storix.storix_api.domains.topicroom.domain.TopicRoom;
import com.storix.storix_api.domains.topicroom.dto.TopicRoomResponseDto;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TopicRoomRepository extends JpaRepository<TopicRoom, Long>, TopicRoomRankingRepository {

    @Query("""
        SELECT new com.storix.storix_api.domains.topicroom.dto.TopicRoomResponseDto(
            t.id, t.topicRoomName, w.worksType, w.worksName, w.thumbnailUrl, t.activeUserNumber, t.lastChatTime, false
        )
        FROM TopicRoom t
        JOIN Works w ON t.worksId = w.id
        WHERE w.id IN :worksIds OR t.topicRoomName LIKE %:keyword%
    """)
    Slice<TopicRoomResponseDto> findBySearchCondition(@Param("worksIds") List<Long> worksIds, @Param("keyword") String keyword, Pageable pageable);


    @Query("""
        SELECT new com.storix.storix_api.domains.topicroom.dto.TopicRoomResponseDto(
            t.id, t.topicRoomName, w.worksType, w.worksName, w.thumbnailUrl, t.activeUserNumber, t.lastChatTime, false
        )
        FROM TopicRoom t
        JOIN Works w ON t.worksId = w.id
        WHERE t.createdAt > :threshold
        ORDER BY t.activeUserNumber DESC
    """)
    List<TopicRoomResponseDto> findTop3TrendingWithWorks(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    @Query("""
        SELECT new com.storix.storix_api.domains.topicroom.dto.TopicRoomResponseDto(
            t.id, t.topicRoomName, w.worksType, w.worksName, w.thumbnailUrl, t.activeUserNumber, t.lastChatTime, false
        )
        FROM TopicRoom t
        JOIN Works w ON t.worksId = w.id
        WHERE t.id NOT IN :excludeIds
        ORDER BY t.activeUserNumber DESC
    """)
    List<TopicRoomResponseDto> findTopAllTimeExcludingWithWorks(@Param("excludeIds") List<Long> excludeIds, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TopicRoom t SET t.activeUserNumber = t.activeUserNumber + 1 WHERE t.id = :id")
    void incrementActiveUserNumber(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TopicRoom t SET t.activeUserNumber = t.activeUserNumber - 1 " +
            "WHERE t.id = :id AND t.activeUserNumber > 0")
    int decrementActiveUserNumber(@Param("id") Long id);

    // 마지막 채팅 시간 갱신
    @Modifying(clearAutomatically = true)
    @Query("UPDATE TopicRoom t SET t.lastChatTime = :lastChatTime WHERE t.id = :roomId")
    void updateLastChatTime(@Param("roomId") Long roomId, @Param("lastChatTime") LocalDateTime lastChatTime);

    // 인기도 순으로 조회
    List<TopicRoom> findTop5ByOrderByPopularityScoreDescLastChatTimeDesc();

    boolean existsByWorksId(Long worksId);

    @Query("SELECT tr FROM TopicRoom tr WHERE tr.activeUserNumber > 1")
    List<TopicRoom> findAllActiveRooms();
}