package com.storix.domain.domains.preference.adaptor;

import com.storix.domain.domains.preference.domain.PreferenceExploration;
import com.storix.domain.domains.preference.dto.ExplorationReactionWithCreatedAt;
import com.storix.domain.domains.preference.repository.ExplorationRepository;
import com.storix.domain.domains.works.dto.WorksMoveCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExplorationAdaptor {

    private final ExplorationRepository explorationRepository;

    public List<ExplorationReactionWithCreatedAt> findExplorationsWithCreatedAtByUserId(Long userId) {
        return explorationRepository.findExplorationsWithCreatedAtByUserId(userId);
    }

    public List<Long> findRespondedWorksIdsByUserId(Long userId) {
        return explorationRepository.findRespondedWorksIdsByUserId(userId);
    }

    public List<Long> findRespondedWorksIdsByStatusSince(Long userId, boolean isLiked, LocalDateTime threshold) {
        return explorationRepository.findRespondedWorksIdsByStatusToday(userId, isLiked, threshold);
    }

    public int countSince(Long userId, LocalDateTime threshold) {
        return explorationRepository.countByUserIdAndCreatedAtAfter(userId, threshold);
    }

    public boolean exists(Long userId, Long worksId) {
        return explorationRepository.existsByUserIdAndWorksId(userId, worksId);
    }

    public void saveAll(List<PreferenceExploration> explorations) {
        explorationRepository.saveAll(explorations);
    }

    public WorksMoveCount moveWorks(Long fromWorksId, Long toWorksId) {
        List<Long> existingUserIds = explorationRepository.findUserIdsByWorksId(toWorksId);
        int removed = existingUserIds.isEmpty() ? 0 : explorationRepository.deleteByWorksIdAndUserIds(fromWorksId, existingUserIds);
        return new WorksMoveCount(explorationRepository.moveWorks(fromWorksId, toWorksId), removed);
    }
}
