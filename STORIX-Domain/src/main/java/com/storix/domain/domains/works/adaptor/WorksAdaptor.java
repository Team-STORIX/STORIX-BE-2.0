package com.storix.domain.domains.works.adaptor;

import com.storix.domain.domains.event.dto.StoryCardLuckyWorkPick;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import com.storix.domain.domains.works.repository.WorksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorksAdaptor {

    private final WorksRepository worksRepository;

    public long countAllWorks() {
        return worksRepository.count();
    }

    public Map<Long, TopicRoomWorksInfo> loadWorksMapByIds(List<Long> worksIds) {

        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TopicRoomWorksInfo> infos = worksRepository.findSimpleInfoByIdIn(worksIds);

        return infos.stream()
                .collect(Collectors.toMap(TopicRoomWorksInfo::id, Function.identity()));
    }

    public void updateDecrementingReviewInfo(Long worksId, double rating) {
        worksRepository.decrementReviewsCountAndUpdateAverageRating(worksId, rating);
    }

    public Optional<StoryCardLuckyWorkPick> pickStoryCardLuckyWork(Genre genre) {

        Map<Long, List<StoryCardLuckyWorkPick>> candidatesByWorks =
                worksRepository.findStoryCardLuckyWorksByGenre(genre).stream()
                        .collect(Collectors.groupingBy(StoryCardLuckyWorkPick::worksId,
                                LinkedHashMap::new, Collectors.toList()));

        if (candidatesByWorks.isEmpty()) {
            return Optional.empty();
        }

        List<Long> worksIds = List.copyOf(candidatesByWorks.keySet());
        List<StoryCardLuckyWorkPick> picked =
                candidatesByWorks.get(worksIds.get(ThreadLocalRandom.current().nextInt(worksIds.size())));

        return picked.stream()
                .filter(candidate -> candidate.landingUrl() != null && !candidate.landingUrl().isBlank())
                .findFirst()
                .or(() -> Optional.of(picked.get(0)));
    }
}
