package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.adaptor.AppEventParticipantAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppEventParticipantService {

    private final AppEventParticipantAdaptor appEventParticipantAdaptor;

    @Transactional
    public void markWinners(Long appEventId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;

        userIds.stream()
                .distinct()
                .forEach(userId -> appEventParticipantAdaptor.upsertWinner(appEventId, userId));
    }
}
