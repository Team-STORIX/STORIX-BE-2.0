package com.storix.domain.domains.event.adaptor;

import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.exception.AppEventNotFoundException;
import com.storix.domain.domains.event.repository.AppEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppEventAdaptor {

    private final AppEventRepository appEventRepository;

    public AppEvent save(AppEvent appEvent) {
        return appEventRepository.save(appEvent);
    }

    public AppEvent findById(Long appEventId) {
        return appEventRepository.findById(appEventId)
                .orElseThrow(() -> AppEventNotFoundException.EXCEPTION);
    }

    public Optional<AppEvent> findOptionalById(Long appEventId) {
        return appEventRepository.findById(appEventId);
    }

    public Page<AppEvent> findAll(Pageable pageable) {
        return appEventRepository.findAllByOrderByIdDesc(pageable);
    }
}
