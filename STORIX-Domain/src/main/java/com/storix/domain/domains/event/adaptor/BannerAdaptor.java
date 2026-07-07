package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.Banner;
import com.storix.domain.domains.event.domain.BannerStatus;
import com.storix.domain.domains.event.exception.BannerNotFoundException;
import com.storix.domain.domains.event.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BannerAdaptor {

    private final BannerRepository eventBannerRepository;

    public Banner save(Banner banner) {
        return eventBannerRepository.save(banner);
    }

    public Banner findById(Long bannerId) {
        return eventBannerRepository.findById(bannerId)
                .orElseThrow(() -> BannerNotFoundException.EXCEPTION);
    }

    public Page<Banner> findAll(Pageable pageable) {
        return eventBannerRepository.findAllByOrderByIdDesc(pageable);
    }

    public Optional<Banner> findActiveBanner(LocalDateTime now) {
        return eventBannerRepository.findActiveBanner(BannerStatus.ACTIVE, now);
    }

    public List<Banner> findDueToActivate(LocalDateTime now) {
        return eventBannerRepository.findAllByStatusAndDisplayStartAtLessThanEqual(BannerStatus.SCHEDULED, now);
    }

    public List<Banner> findDueToEnd(LocalDateTime now) {
        return eventBannerRepository.findAllByStatusAndDisplayEndAtLessThan(BannerStatus.ACTIVE, now);
    }

    public boolean existsOverlapping(LocalDateTime displayStartAt, LocalDateTime displayEndAt, Long excludeId) {
        return eventBannerRepository.existsOverlappingActiveBanner(displayStartAt, displayEndAt, excludeId);
    }

    // AppEvent 강제 종료 시 cascade 대상
    public List<Banner> findActiveByAppEvent(Long appEventId) {
        return eventBannerRepository.findAllByAppEvent_IdAndStatusNot(appEventId, BannerStatus.ENDED);
    }
}
