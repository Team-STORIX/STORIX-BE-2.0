package com.storix.domain.domains.hashtag.adaptor;

import com.storix.domain.domains.hashtag.application.port.LoadHashtagPort;
import com.storix.domain.domains.hashtag.dto.HashtagInfo;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.hashtag.repository.HashtagRepository;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HashtagAdaptor implements LoadHashtagPort {

    private final HashtagRepository hashtagRepository;

    public Map<Long, List<String>> findHashTagsByWorksIds (List<Long> worksIds){
        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<HashtagInfo> hashtags = hashtagRepository.findAllByWorksIds(worksIds);

        return hashtags.stream()
                .collect(Collectors.groupingBy(
                        HashtagInfo::worksId,
                        LinkedHashMap::new,
                        Collectors.mapping(HashtagInfo::hashtagName, Collectors.toList())
                ));
    }

    @Override
    public List<HashtagRecommendResponseDto> recommendByGenres(Set<Genre> genres, int limit) {
        return hashtagRepository.findPopularByGenres(genres, PageRequest.of(0, limit));
    }

    @Override
    public List<HashtagRecommendResponseDto> recommendGlobalPopular(int limit) {
        return hashtagRepository.findGlobalPopular(PageRequest.of(0, limit));
    }
}
