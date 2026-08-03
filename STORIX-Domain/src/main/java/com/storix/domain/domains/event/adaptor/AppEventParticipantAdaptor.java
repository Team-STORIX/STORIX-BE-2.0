package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.repository.AppEventParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AppEventParticipantAdaptor {

    private final AppEventParticipantRepository appEventParticipantRepository;

    public List<Long> findWinnerUserIds(Long appEventId, Long lastUserId, Pageable pageable) {
        return appEventParticipantRepository.findWinnerUserIds(appEventId, lastUserId, pageable);
    }

    public void upsertWinner(Long appEventId, Long userId) {
        appEventParticipantRepository.upsertWinner(appEventId, userId);
    }
}
