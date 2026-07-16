package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.PopupDismissAdaptor;
import com.storix.domain.domains.event.domain.PopupExposurePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PopupDismissService {

    private final PopupDismissAdaptor popupDismissAdaptor;

    // 원자적 upsert, 동시 요청에도 멱등
    @Transactional
    public void dismissForToday(Long userId, Long popupId, LocalDate today) {
        popupDismissAdaptor.upsertForToday(userId, popupId, today);
    }

    @Transactional(readOnly = true)
    public boolean isSuppressed(Long userId, Long popupId, PopupExposurePolicy policy, LocalDate today) {
        return switch (policy) {
            case ALWAYS_DURING_PERIOD -> false;                                             // 닫기 버튼만 - 억제 없이 기간 내 항상 노출
            case ONCE_PER_DAY -> popupDismissAdaptor.isDismissedOn(userId, popupId, today); // 오늘 dismiss 했으면 숨김
        };
    }
}
