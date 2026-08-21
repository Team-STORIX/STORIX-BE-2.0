package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.BannerModalConfirmationAdaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerModalService {

    private final BannerModalConfirmationAdaptor bannerModalConfirmationAdaptor;

    // 해당 배너를 한 번도 확인하지 않은 유저인지 (배너 종류 무관, bannerId로 구분)
    @Transactional(readOnly = true)
    public boolean isModalRequired(Long userId, Long bannerId) {
        return !bannerModalConfirmationAdaptor.isConfirmed(userId, bannerId);
    }

    // 원자적 upsert, 동시 요청에도 멱등
    @Transactional
    public void confirm(Long userId, Long bannerId) {
        bannerModalConfirmationAdaptor.confirm(userId, bannerId);
    }
}
