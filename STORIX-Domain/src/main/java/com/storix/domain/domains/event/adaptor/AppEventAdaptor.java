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

    public Page<AppEvent> findAll(Pageable pageable) {
        return appEventRepository.findAllByOrderByIdDesc(pageable);
    }

    public Page<AppEvent> searchByName(String keyword, Pageable pageable) {
        return appEventRepository.searchByName(keyword, pageable);
    }

    /**
     * 전용 API가 대상 이벤트를 찾는 경로.
     *
     * 진행 중인 이벤트를 우선 반환하고, 없으면 해당 타입의 가장 최근 이벤트로 폴백한다.
     * 폴백이 있어야 기간 종료 후에도 이벤트 기간을 내려주며 eventActive=false로 응답할 수 있다.
     */
    public Optional<AppEvent> findActiveOrLatestByType(AppEventType eventType, LocalDateTime now) {
        return appEventRepository.findActiveByType(eventType, now).stream()
                .findFirst()
                .or(() -> appEventRepository.findFirstByEventTypeOrderByStartAtDesc(eventType));
    }

    /**
     * 같은 타입 이벤트끼리 기간이 겹치는지 확인하면서 해당 구간에 쓰기 락
     */
    public boolean lockAndCheckOverlappingByType(AppEventType eventType,
                                                 LocalDateTime startAt,
                                                 LocalDateTime endAt,
                                                 Long excludeAppEventId) {
        return appEventRepository.findOverlappingByTypeForUpdate(eventType, startAt, endAt).stream()
                .anyMatch(overlapping -> !overlapping.getId().equals(excludeAppEventId));
    }
}
