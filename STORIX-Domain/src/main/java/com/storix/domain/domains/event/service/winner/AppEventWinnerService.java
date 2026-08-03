package com.storix.domain.domains.event.service.winner;

import com.storix.domain.domains.event.adaptor.AppEventWinnerAdaptor;
import com.storix.domain.domains.event.dto.EventWinner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppEventWinnerService {

    private final AppEventWinnerAdaptor appEventWinnerAdaptor;

    @Transactional(readOnly = true)
    public List<EventWinner> findWinners(Long appEventId) {
        return appEventWinnerAdaptor.findWinners(appEventId);
    }

    @Transactional
    public void saveWinners(Long appEventId, List<EventWinner> winners) {
        if (winners == null || winners.isEmpty()) return;

        winners.forEach(winner ->
                appEventWinnerAdaptor.insertWinnerIfAbsent(appEventId, winner.userId(), winner.drawOrder()));
    }
}
