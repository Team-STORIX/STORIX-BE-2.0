package com.storix.domain.domains.works.adaptor;

import com.storix.domain.domains.event.dto.StoryCardLuckyWorkPick;
import com.storix.domain.domains.plus.exception.WorksNotExistException;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.LibraryWorksInfo;
import com.storix.domain.domains.works.dto.SlicedWorksInfo;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import com.storix.domain.domains.works.dto.WorksInfo;
import com.storix.domain.domains.works.exception.UnknownWorksException;
import com.storix.domain.domains.works.repository.WorksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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

    // 작품 검색
    public Slice<Works> searchWorks(String keyword, Pageable pageable) {
        return worksRepository.findBySearchKeyword(keyword, pageable);
    }

    public Slice<Works> searchWorksWithFilters(String keyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable) {
        return worksRepository.searchWithFilters(keyword, worksTypes, genres, pageable);
    }

    public Slice<Works> searchWorksByHashtagWithFilters(String hashtagKeyword, List<WorksType> worksTypes, List<Genre> genres, Pageable pageable) {
        return worksRepository.searchByHashtagWithFilters(hashtagKeyword, worksTypes, genres, pageable);
    }

    // 작품 조회
    public Works findById(Long worksId) {
        return worksRepository.findById(worksId)
                .orElseThrow(() -> UnknownWorksException.EXCEPTION);
    }

    public List<Long> findAllIdsByKeyword(String keyword) {
        return worksRepository.findAllIdsByKeyword(keyword);
    }

    public List<Long> findAllIdsByKeywordWithFilters(String keyword, List<WorksType> worksTypes, List<Genre> genres) {
        return worksRepository.searchIdsWithFilters(keyword, worksTypes, genres);
    }

    public Works findByIdWithHashtags(Long worksId) {
        return worksRepository.findByIdWithHashtags(worksId)
                .orElseThrow(() -> UnknownWorksException.EXCEPTION);
    }

    // 리뷰 도메인 용
    public void checkWorksExistById(Long worksId) {
        if (!worksRepository.existsById(worksId)) {
            throw WorksNotExistException.EXCEPTION;
        }
    }

    public Boolean isWorksForAdult(Long worksId) {
        return worksRepository.isWorksForAdult(worksId);
    }

    public void updateIncrementingReviewInfo(Long worksId, double newRating) {
        worksRepository.incrementReviewsCountAndUpdateAverageRating(worksId, newRating);
    }


    // 서재 도메인 용
    public List<LibraryWorksInfo> getLibraryWorksInfo(List<Long> worksIds) {
        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyList();
        }

        return worksRepository.findLibraryWorksInfoByIds(worksIds);
    }

    public Slice<LibraryWorksInfo> searchLibraryWorksInfoByIds(List<Long> worksIds, String keyword, Pageable pageable) {
        if (worksIds == null || worksIds.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        return worksRepository.searchLibraryWorksInfoByIds(worksIds, keyword, pageable);
    }

    // 작품 정보 조회용
    public Map<Long, WorksInfo> findAllWorksInfoByWorksIds(List<Long> worksIds) {
        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<WorksInfo> worksInfos = worksRepository.findWorksInfoByIds(worksIds);
        worksInfos.forEach(this::warnIfEnumMissing);

        return worksInfos.stream()
                .collect(Collectors.toMap(
                        WorksInfo::worksId,
                        Function.identity()
                ));
    }

    // 작품 상세 리뷰용
    public WorksInfo findWorksInfoById(Long worksId) {
        Optional<WorksInfo> worksInfo = worksRepository.findWorksInfoById(worksId);
        if (worksInfo.isEmpty()) {
            throw UnknownWorksException.EXCEPTION;
        }
        warnIfEnumMissing(worksInfo.get());
        return worksInfo.get();
    }

    private void warnIfEnumMissing(WorksInfo works) {
        if (works.worksType() != null && works.genre() != null) return;
        log.atWarn()
                .addKeyValue("worksId", works.worksId())
                .addKeyValue("worksTypeMissing", works.worksType() == null)
                .addKeyValue("genreMissing", works.genre() == null)
                .log(">>> [Works] enum 컬럼 값 없음");
    }

    // 관심 작품 리스트 조회 용
    public Map<Long, SlicedWorksInfo> findAllSlicedWorksInfoByWorksIds(List<Long> worksIds) {
        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SlicedWorksInfo> slicedWorksInfos = worksRepository.findAllSlicedWorksInfoByWorksIds(worksIds);

        return slicedWorksInfos.stream()
                .collect(Collectors.toMap(
                        SlicedWorksInfo::worksId,
                        Function.identity()
                ));
    }

    public List<Works> findRandomWorksExcluding(List<Long> excludedIds, int needed, boolean excludeAdult) {

        List<Long> candidateIds = worksRepository.findCandidateIds(excludedIds, excludeAdult);

        if (candidateIds.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(candidateIds);

        List<Long> targetIds = candidateIds.stream()
                .limit(needed)
                .toList();

        return worksRepository.findAllByIdWithHashtags(targetIds);
    }

    public List<Works> findWorksByIds(List<Long> worksIds) {

        if (worksIds == null || worksIds.isEmpty()) {
            return Collections.emptyList();
        }

        return worksRepository.findAllById(worksIds);
    }
}
