package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.exception.AppEventNotFoundException;
import com.storix.domain.domains.event.repository.AppEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppEventAdaptor {

    private final AppEventRepository appEventRepository;

    public AppEvent save(AppEvent appEvent) {
        return appEventRepository.save(appEvent);
    }

    public AppEvent findById(Long appEventId) {
        return appEventRepository.findById(appEventId)
                .orElseThrow(() -> AppEventNotFoundException.EXCEPTION);
    }

    public Optional<AppEvent> findOptionalById(Long appEventId) {
        return appEventRepository.findById(appEventId);
    }

    public AppEvent findByIdForUpdate(Long appEventId) {
        return appEventRepository.findByIdForUpdate(appEventId)
                .orElseThrow(() -> AppEventNotFoundException.EXCEPTION);
    }

    public Page<AppEvent> findAll(Pageable pageable) {
        return appEventRepository.findAllByOrderByIdDesc(pageable);
    }

    public Page<AppEvent> searchByName(String keyword, Pageable pageable) {
        return appEventRepository.searchByName(keyword, pageable);
    }

    // 진행 중 → 가장 최근에 종료된 → 가장 먼저 시작하는 예정 이벤트 순으로 폴백
    public Optional<AppEvent> findActiveOrNearestByType(AppEventType eventType, LocalDateTime now) {
        return appEventRepository.findActiveByType(eventType, now).stream()
                .findFirst()
                .or(() -> appEventRepository.findFirstByEventTypeAndEndAtLessThanEqualOrderByEndAtDesc(eventType, now))
                .or(() -> appEventRepository.findFirstByEventTypeAndStartAtGreaterThanOrderByStartAtAsc(eventType, now));
    }

    // 같은 타입 이벤트끼리 기간이 겹치는지 확인하면서 해당 구간에 쓰기 락
    public boolean lockAndCheckOverlappingByType(AppEventType eventType,
                                                 LocalDateTime startAt,
                                                 LocalDateTime endAt,
                                                 Long excludeAppEventId) {
        return appEventRepository.findOverlappingByTypeForUpdate(eventType, startAt, endAt).stream()
                .anyMatch(overlapping -> !overlapping.getId().equals(excludeAppEventId));
    }
}
