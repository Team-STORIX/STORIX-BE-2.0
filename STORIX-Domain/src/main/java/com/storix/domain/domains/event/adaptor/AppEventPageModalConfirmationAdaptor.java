package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.repository.AppEventPageModalConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppEventPageModalConfirmationAdaptor {

    private final AppEventPageModalConfirmationRepository appEventPageModalConfirmationRepository;

    public boolean isConfirmed(Long userId, Long appEventId) {
        return appEventPageModalConfirmationRepository.existsByUserIdAndAppEventId(userId, appEventId);
    }

    public void confirm(Long userId, Long appEventId) {
        appEventPageModalConfirmationRepository.insertIfAbsent(userId, appEventId);
    }
}
