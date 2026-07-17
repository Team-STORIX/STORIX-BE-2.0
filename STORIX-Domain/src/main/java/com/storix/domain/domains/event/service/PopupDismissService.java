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

    // '다시 보지 않기' - 노출 기간 내내 미노출
    @Transactional
    public void dismissForever(Long userId, Long popupId, LocalDate today) {
        popupDismissAdaptor.upsertNeverShow(userId, popupId, today);
    }

    @Transactional(readOnly = true)
    public boolean isSuppressed(Long userId, Long popupId, PopupExposurePolicy policy, LocalDate today) {
        return switch (policy) {
            case ALWAYS_DURING_PERIOD -> popupDismissAdaptor.isPermanentlyDismissed(userId, popupId); // 닫기 버튼만 - '다시 보지 않기'한 유저만 숨김
            case ONCE_PER_DAY -> popupDismissAdaptor.isSuppressedOn(userId, popupId, today);          // 오늘 dismiss 또는 영구 dismiss 시 숨김
        };
    }
}
