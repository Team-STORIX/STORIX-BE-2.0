package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.dto.EventWinner;
import com.storix.domain.domains.event.repository.AppEventWinnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AppEventWinnerAdaptor {

    private final AppEventWinnerRepository appEventWinnerRepository;

    public List<Long> findWinnerUserIds(Long appEventId, Long lastUserId, Pageable pageable) {
        return appEventWinnerRepository.findWinnerUserIds(appEventId, lastUserId, pageable);
    }

    public List<EventWinner> findWinners(Long appEventId) {
        return appEventWinnerRepository.findWinners(appEventId);
    }

    public void insertWinnerIfAbsent(Long appEventId, Long userId, int drawOrder) {
        appEventWinnerRepository.insertWinnerIfAbsent(appEventId, userId, drawOrder);
    }
}
