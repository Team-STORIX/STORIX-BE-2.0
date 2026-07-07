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
        popupDismissRepository.upsertDismiss(userId, popupId, today);
    }

    public boolean isDismissedOn(Long userId, Long popupId, LocalDate date) {
        return popupDismissRepository.existsByUserIdAndPopup_IdAndDismissedOn(userId, popupId, date);
    }
}
