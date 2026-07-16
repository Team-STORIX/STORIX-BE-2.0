package com.storix.domain.domains.preference.adaptor;

import com.storix.domain.domains.preference.dto.ExplorationReactionWithCreatedAt;
import com.storix.domain.domains.preference.repository.ExplorationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExplorationAdaptor {

    private final ExplorationRepository explorationRepository;

    public List<ExplorationReactionWithCreatedAt> findExplorationsWithCreatedAtByUserId(Long userId) {
        return explorationRepository.findExplorationsWithCreatedAtByUserId(userId);
    }
}
