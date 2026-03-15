package com.storix.domain.domains.topicroom.application.port;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;
import java.util.List;

public interface LoadTopicRoomPort {

    TopicRoom findById(Long roomId);

    Slice<TopicRoomUser> findParticipationsByUserId(Long userId, Pageable pageable);

    List<TopicRoomResponseDto> findTop3TrendingWithWorks(LocalDateTime threshold);

    List<TopicRoomResponseDto> findTopAllTimeExcludingWithWorks(int limit, List<Long> excludeIds);

    Slice<TopicRoomResponseDto> searchBySearchCondition(List<Long> worksIds, String keyword, Pageable pageable);

    List<Long> findAllJoinedRoomIdsByUserId(Long userId);

    long countJoinedRooms(Long userId);

    boolean existsByWorksId(Long worksId);

    boolean existsById(Long roomId);

    List<TopicRoom> loadTop5PopularRooms();

    List<TopicRoom> findAllActiveRooms();
}