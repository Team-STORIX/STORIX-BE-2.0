package com.storix.domain.domains.works.adaptor;

import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import com.storix.domain.domains.works.exception.UnknownWorksException;
import com.storix.domain.domains.works.repository.WorksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorksAdaptor {

    private final WorksRepository worksRepository;

    public Works findById(Long worksId) {
        return worksRepository.findById(worksId)
                .orElseThrow(() -> UnknownWorksException.EXCEPTION);
    }

    // 작품 ID 리스트로 작품 정보 맵 로드
    public Map<Long, TopicRoomWorksInfo> loadWorksMapByIds(List<Long> worksIds) {

        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TopicRoomWorksInfo> infos = worksRepository.findSimpleInfoByIdIn(worksIds);

        return infos.stream()
                .collect(Collectors.toMap(TopicRoomWorksInfo::id, Function.identity()));
    }

    public List<Long> findAllIdsByKeyword(String keyword) {
        return worksRepository.findAllIdsByKeyword(keyword);
    }

    // 토픽룸 도메인 용 (키워드 + 필터로 작품 ID 리스트 조회)
    public List<Long> findAllIdsByKeywordWithFilters(String keyword, List<WorksType> worksTypes, List<Genre> genres) {
        return worksRepository.searchIdsWithFilters(keyword, worksTypes, genres);
    }
}
