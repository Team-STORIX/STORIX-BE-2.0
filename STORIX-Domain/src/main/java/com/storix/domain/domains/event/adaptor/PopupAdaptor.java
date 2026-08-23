package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.Popup;
import com.storix.domain.domains.event.domain.PopupStatus;
import com.storix.domain.domains.event.exception.PopupNotFoundException;
import com.storix.domain.domains.event.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PopupAdaptor {

    private final PopupRepository eventPopupRepository;

    public Popup save(Popup popup) {
        return eventPopupRepository.save(popup);
    }

    public Popup findById(Long popupId) {
        return eventPopupRepository.findById(popupId)
                .orElseThrow(() -> PopupNotFoundException.EXCEPTION);
    }

    public Page<Popup> findAll(Pageable pageable) {
        return eventPopupRepository.findAllByOrderByIdDesc(pageable);
    }

    public Page<Popup> searchByTitle(String keyword, Pageable pageable) {
        return eventPopupRepository.searchByPopupTitle(keyword, pageable);
    }

    public Optional<Popup> findActivePopup(LocalDateTime now) {
        return eventPopupRepository.findActivePopup(PopupStatus.ACTIVE, now);
    }

    public List<Popup> findDueToActivate(LocalDateTime now) {
        return eventPopupRepository.findAllByStatusAndDisplayStartAtLessThanEqual(PopupStatus.SCHEDULED, now);
    }

    public List<Popup> findDueToEnd(LocalDateTime now) {
        return eventPopupRepository.findAllByStatusAndDisplayEndAtLessThan(PopupStatus.ACTIVE, now);
    }

    public boolean existsOverlapping(LocalDateTime displayStartAt, LocalDateTime displayEndAt, Long excludeId) {
        return eventPopupRepository.existsOverlappingActivePopup(displayStartAt, displayEndAt, excludeId);
    }

    // AppEvent 강제 종료 시 cascade 대상
    public List<Popup> findActiveByAppEvent(Long appEventId) {
        return eventPopupRepository.findAllByAppEvent_IdAndStatusNot(appEventId, PopupStatus.ENDED);
    }
}
