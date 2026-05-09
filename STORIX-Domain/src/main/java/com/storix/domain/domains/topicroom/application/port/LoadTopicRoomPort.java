package com.storix.domain.domains.topicroom.application.port;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import org.springframework.data.domain.*;
import java.util.List;

public interface LoadTopicRoomPort {

    Slice<TopicRoomResponseDto> searchWithFilters(List<Long> worksIds, Pageable pageable);

    boolean existsByWorksId(Long worksId);

    boolean existsById(Long roomId);

    List<TopicRoom> findAllActiveRooms();
}