package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventPageModalConfirmationAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppEventPageModalService {

    private final AppEventPageModalConfirmationAdaptor appEventPageModalConfirmationAdaptor;

    // 해당 이벤트 상세 페이지를 한 번도 확인하지 않은 유저인지 (프로모션 수단 무관, appEventId로 구분)
    @Transactional(readOnly = true)
    public boolean isModalRequired(Long userId, Long appEventId) {
        return !appEventPageModalConfirmationAdaptor.isConfirmed(userId, appEventId);
    }

    // 원자적 upsert, 동시 요청에도 멱등
    @Transactional
    public void confirm(Long userId, Long appEventId) {
        appEventPageModalConfirmationAdaptor.confirm(userId, appEventId);
    }
}
