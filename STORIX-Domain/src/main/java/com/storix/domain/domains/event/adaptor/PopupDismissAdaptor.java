package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.repository.PopupDismissRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PopupDismissAdaptor {

    private final PopupDismissRepository popupDismissRepository;

    public void upsertForToday(Long userId, Long popupId, LocalDate today) {
        popupDismissRepository.upsertDismiss(userId, popupId, today, false);
    }

    public void upsertNeverShow(Long userId, Long popupId, LocalDate today) {
        popupDismissRepository.upsertDismiss(userId, popupId, today, true);
    }

    public boolean isPermanentlyDismissed(Long userId, Long popupId) {
        return popupDismissRepository.existsByUserIdAndPopup_IdAndPermanentTrue(userId, popupId);
    }

    public boolean isSuppressedOn(Long userId, Long popupId, LocalDate date) {
        return popupDismissRepository.existsSuppressedOn(userId, popupId, date);
    }
}
