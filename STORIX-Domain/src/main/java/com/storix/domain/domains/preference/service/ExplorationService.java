package com.storix.domain.domains.preference.service;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.domain.domains.preference.application.ExplorationUseCase;
import com.storix.domain.domains.preference.dto.*;
import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.preference.exception.DuplicatedExplorationException;
import com.storix.domain.domains.preference.repository.ExplorationRepository;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.LibraryWorksInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
public class ExplorationService implements ExplorationUseCase {

    private final ExplorationRepository explorationRepository;
    private final LoadWorksPort loadWorksPort;
    private final ExplorationCacheHelper cacheHelper;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;

    @Override
    @Transactional(readOnly = true)
    public List<ExplorationWorksResponseDto> getExplorationWorks(Long userId) {
        if (cacheHelper.isAlreadyParticipatedToday(userId)) {
            return Collections.emptyList();
        }

        LocalDateTime threshold = getSessionThreshold();

        List<Long> dbHistoryIds = explorationRepository.findRespondedWorksIdsByUserId(userId);
        Set<Long> pendingIds = cacheHelper.getPendingWorksIds(userId);
        List<Long> favoriteWorksIds = favoriteWorksAdaptor.findAllFavoriteWorksIdsByUserId(userId);

        Set<Long> allHistoryIds = new HashSet<>(dbHistoryIds);
        allHistoryIds.addAll(pendingIds);
        allHistoryIds.addAll(favoriteWorksIds);

        int sessionCount = explorationRepository.countByUserIdAndCreatedAtAfter(userId, threshold)
                + pendingIds.size();

        int needed = 10 - sessionCount;
        if (needed <= 0) return Collections.emptyList();

        return loadWorksPort.findRandomWorksExcluding(new ArrayList<>(allHistoryIds), needed)
                .stream()
                .map(ExplorationWorksResponseDto::from)
                .toList();
    }

    @Override
    @Transactional
    public void submitExploration(Long userId, ExplorationSubmitRequestDto request) {

        loadWorksPort.checkWorksExistById(request.worksId());

        if (cacheHelper.isAlreadyParticipatedToday(userId)) {
            throw DuplicatedExplorationException.EXCEPTION;
        }

        PendingSwipeDto pendingDto = PendingSwipeDto.builder()
                .userId(userId)
                .worksId(request.worksId())
                .isLiked(request.isLiked())
                .build();

        Long result = cacheHelper.submitWithLua(userId, pendingDto);

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

        if (result == 15) {
            cacheHelper.markAsParticipatedToday(userId);
            cacheHelper.deleteChartCache(userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExplorationResultResponseDto getExplorationResults(Long userId) {

        LocalDateTime threshold = LocalDateTime.now().minusHours(3);

        List<Long> dbLikedIds = explorationRepository.findRespondedWorksIdsByStatusToday(userId, true, threshold);
        List<Long> dbDislikedIds = explorationRepository.findRespondedWorksIdsByStatusToday(userId, false, threshold);

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

        List<Works> allLiked = loadWorksPort.findWorksByIds(new ArrayList<>(finalLikedIds));
        List<Works> allDisliked = loadWorksPort.findWorksByIds(new ArrayList<>(finalDislikedIds));

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
                        w.getAuthor(),
                        w.getIllustrator(),
                        w.getOriginalAuthor(),
                        w.getThumbnailUrl(),
                        w.getWorksType(),
                        w.getGenre()
                ))
                .toList();
    }

    private LocalDateTime getSessionThreshold() {
        return LocalDateTime.now().minusHours(3);
    }
}
