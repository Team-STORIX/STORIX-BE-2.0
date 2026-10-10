package com.storix.domain.domains.preference.service;

import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.preference.dto.*;
import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.preference.exception.DuplicatedExplorationException;
import com.storix.domain.domains.preference.adaptor.ExplorationAdaptor;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.LibraryWorksInfo;
import com.storix.domain.domains.preference.domain.PreferenceExploration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExplorationService {

    public static final int DAILY_EXPLORATION_LIMIT = 10;

    private final WorksAdaptor worksAdaptor;
    private final ExplorationAdaptor explorationAdaptor;
    private final ExplorationCacheHelper cacheHelper;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final AdultVerificationAdaptor adultVerificationAdaptor;

    @Transactional(readOnly = true)
    public List<ExplorationWorksResponseDto> getExplorationWorks(Long userId) {
        if (cacheHelper.isAlreadyParticipatedToday(userId)) {
            return Collections.emptyList();
        }

        LocalDateTime threshold = LocalDateTime.now().minusHours(3);

        List<Long> dbHistoryIds = explorationAdaptor.findRespondedWorksIdsByUserId(userId);
        Set<Long> pendingIds = cacheHelper.getPendingWorksIds(userId);
        List<Long> favoriteWorksIds = favoriteWorksAdaptor.findAllFavoriteWorksIdsByUserId(userId);
        List<Long> reviewedWorksIds = reviewAdaptor.findAllReviewedWorksIdsByUserId(userId);

        Set<Long> allHistoryIds = new HashSet<>(dbHistoryIds);
        allHistoryIds.addAll(pendingIds);
        allHistoryIds.addAll(favoriteWorksIds);
        allHistoryIds.addAll(reviewedWorksIds);

        int sessionCount = explorationAdaptor.countSince(userId, threshold)
                + pendingIds.size();

        int needed = DAILY_EXPLORATION_LIMIT - sessionCount;
        if (needed <= 0) return Collections.emptyList();

        boolean excludeAdult = adultVerificationAdaptor.excludeAdultFor(userId);

        return worksAdaptor.findRandomWorksExcluding(new ArrayList<>(allHistoryIds), needed, excludeAdult)
                .stream()
                .map(ExplorationWorksResponseDto::from)
                .toList();
    }

    @Transactional
    public void submitExploration(Long userId, ExplorationSubmitRequestDto request) {

        worksAdaptor.checkWorksExistById(request.worksId());

        if (cacheHelper.isAlreadyParticipatedToday(userId)) {
            throw DuplicatedExplorationException.EXCEPTION;
        }

        PendingSwipeDto pendingDto = PendingSwipeDto.builder()
                .userId(userId)
                .worksId(request.worksId())
                .isLiked(request.isLiked())
                .build();

        Long result = cacheHelper.submitWithLua(userId, pendingDto, DAILY_EXPLORATION_LIMIT);

        if (result == -1 || result == -2) {
            cacheHelper.markAsParticipatedToday(userId);
            throw DuplicatedExplorationException.EXCEPTION;
        }

        if (result == -3) {
            throw new STORIXCodeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        if (result == -4) {
            throw new STORIXCodeException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        if (result == DAILY_EXPLORATION_LIMIT) {
            cacheHelper.markAsParticipatedToday(userId);
            cacheHelper.deleteChartCache(userId);
        }
    }

    @Transactional(readOnly = true)
    public ExplorationResultResponseDto getExplorationResults(Long userId) {

        LocalDateTime threshold = LocalDateTime.now().minusHours(3);

        List<Long> dbLikedIds = explorationAdaptor.findRespondedWorksIdsByStatusSince(userId, true, threshold);
        List<Long> dbDislikedIds = explorationAdaptor.findRespondedWorksIdsByStatusSince(userId, false, threshold);

        List<PendingSwipeDto> pending = cacheHelper.getAllPendingSwipes(userId);

        Set<Long> finalLikedIds = new HashSet<>(dbLikedIds);
        finalLikedIds.addAll(pending.stream()
                .filter(PendingSwipeDto::isLiked)
                .map(PendingSwipeDto::worksId)
                .collect(Collectors.toSet()));

        Set<Long> finalDislikedIds = new HashSet<>(dbDislikedIds);
        finalDislikedIds.addAll(pending.stream()
                .filter(p -> !p.isLiked())
                .map(PendingSwipeDto::worksId)
                .collect(Collectors.toSet()));

        List<Works> allLiked = worksAdaptor.findWorksByIds(new ArrayList<>(finalLikedIds));
        List<Works> allDisliked = worksAdaptor.findWorksByIds(new ArrayList<>(finalDislikedIds));

        return ExplorationResultResponseDto.builder()
                .likedWorks(toLibraryWorksInfoList(allLiked))
                .dislikedWorks(toLibraryWorksInfoList(allDisliked))
                .build();
    }

    private List<LibraryWorksInfo> toLibraryWorksInfoList(List<Works> worksList) {
        return worksList.stream()
                .map(w -> new LibraryWorksInfo(
                        w.getId(),
                        w.getWorksName(),
                        w.getArtistName(),
                        w.getAuthor(),
                        w.getIllustrator(),
                        w.getOriginalAuthor(),
                        w.getThumbnailUrl(),
                        w.getWorksType(),
                        w.getGenre(),
                        w.getAvgRating(),
                        w.getAgeClassification()
                ))
                .toList();
    }

    // Redis 에 쌓인 스와이프 DB 반영
    @Transactional
    public void flushPendingSwipes(int batchSize) {
        List<PendingSwipeDto> batch = cacheHelper.popBatchFromGlobalQueue(batchSize);

        if (batch.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        int totalCount = batch.size();

        // 중복 제거 필터링
        List<PreferenceExploration> entitiesToSave = batch.stream()
                .filter(dto -> {
                    boolean exists = explorationAdaptor.exists(dto.userId(), dto.worksId());
                    if (exists) {
                        log.debug(">>> [Batch Skip] User {} - Works {} is already recorded.", dto.userId(), dto.worksId());
                    }
                    return !exists;
                })
                .map(dto -> PreferenceExploration.builder()
                        .userId(dto.userId())
                        .worksId(dto.worksId())
                        .isLiked(dto.isLiked())
                        .build())
                .toList();

        int skippedCount = totalCount - entitiesToSave.size();

        if (entitiesToSave.isEmpty()) {
            log.info(">>> [ExplorationBatch] 새 데이터 없음 (Total: {}, Skipped: {})", totalCount, skippedCount);
            return;
        }

        try {
            explorationAdaptor.saveAll(entitiesToSave);

            log.info(">>> [ExplorationBatch] synchronized 성 [Total: {}, Saved: {}, Skipped: {}] ({}ms)",
                    totalCount, entitiesToSave.size(), skippedCount, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error(">>> [ExplorationBatch] Critical save error: {}. Check DB constraints or entity mapping.", e.getMessage());
        }
    }
}
