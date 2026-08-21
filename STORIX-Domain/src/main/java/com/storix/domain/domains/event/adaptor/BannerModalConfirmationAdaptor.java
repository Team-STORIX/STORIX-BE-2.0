package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.repository.BannerModalConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BannerModalConfirmationAdaptor {

    private final BannerModalConfirmationRepository bannerModalConfirmationRepository;

    public boolean isConfirmed(Long userId, Long bannerId) {
        return bannerModalConfirmationRepository.existsByUserIdAndBanner_Id(userId, bannerId);
    }

    public void confirm(Long userId, Long bannerId) {
        bannerModalConfirmationRepository.insertIfAbsent(userId, bannerId);
    }
}
