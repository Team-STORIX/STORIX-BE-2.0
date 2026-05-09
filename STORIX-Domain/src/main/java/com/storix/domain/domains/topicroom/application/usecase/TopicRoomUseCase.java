package com.storix.domain.domains.topicroom.application.usecase;

import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksType;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TopicRoomUseCase {

    // 토픽룸 탭 필터 검색
    PlusSearchResponseWrapperDto<TopicRoomResponseDto> searchRoomsWithFilters(
            Long userId, String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable);

}