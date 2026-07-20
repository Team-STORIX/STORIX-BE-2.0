package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.dto.AppEventCommand;
import com.storix.domain.domains.event.dto.AppEventResponse;
import com.storix.domain.domains.event.exception.AppEventInvalidPeriodException;
import com.storix.domain.domains.event.exception.AppEventNameRequiredException;
import com.storix.domain.domains.event.exception.AppEventPeriodRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppEventService {

    private static final int APP_EVENT_PAGE_SIZE = 10;

    private final AppEventAdaptor appEventAdaptor;
    private final PopupService popupService;
    private final BannerService bannerService;

    // 응답 DTO 매핑은 promotionTypes(LAZY) 초기화를 위해 트랜잭션 안에서 수행한다
    @Transactional
    public AppEventResponse create(AppEventCommand cmd, Long adminUserId) {
        validateCommand(cmd);
        AppEvent saved = appEventAdaptor.save(AppEvent.builder()
                .name(cmd.name())
                .description(cmd.description())
                .startAt(cmd.startAt())
                .endAt(cmd.endAt())
                .hasWinner(cmd.hasWinner())
                .promotionTypes(cmd.promotionTypes())
                .assigneeAdminId(adminUserId)
                .build());
        return AppEventResponse.from(saved);
    }

    @Transactional
    public AppEventResponse update(Long appEventId, AppEventCommand cmd) {
        validateCommand(cmd);
        AppEvent appEvent = appEventAdaptor.findById(appEventId);
        boolean periodChanged = !appEvent.getStartAt().equals(cmd.startAt())
                || !appEvent.getEndAt().equals(cmd.endAt());
        appEvent.update(
                cmd.name(),
                cmd.description(),
                cmd.startAt(),
                cmd.endAt(),
                cmd.hasWinner(),
                cmd.promotionTypes()
        );
        // 이벤트 기간이 바뀌면 소속 팝업/배너 노출기간을 이벤트 기간 안으로 clamp (앱 이벤트 ⊇ 팝업/배너)
        if (periodChanged) {
            popupService.clampByAppEvent(appEventId, cmd.startAt(), cmd.endAt());
            bannerService.clampByAppEvent(appEventId, cmd.startAt(), cmd.endAt());
        }
        return AppEventResponse.from(appEvent);
    }

    @Transactional(readOnly = true)
    public AppEventResponse getAppEvent(Long appEventId) {
        return AppEventResponse.from(appEventAdaptor.findById(appEventId));
    }

    @Transactional(readOnly = true)
    public Page<AppEventResponse> getAppEvents(int page, String keyword) {
        String normalized = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return appEventAdaptor.searchByName(normalized, PageRequest.of(page, APP_EVENT_PAGE_SIZE))
                .map(AppEventResponse::from);
    }

    // 팝업/배너도 함께 종료
    @Transactional
    public AppEventResponse cancel(Long appEventId) {
        AppEvent appEvent = appEventAdaptor.findById(appEventId);
        appEvent.endNow(LocalDateTime.now());
        popupService.endByAppEvent(appEventId);
        bannerService.endByAppEvent(appEventId);
        return AppEventResponse.from(appEvent);
    }

    private void validateCommand(AppEventCommand cmd) {
        if (cmd.name() == null || cmd.name().isBlank()) {
            throw AppEventNameRequiredException.EXCEPTION;
        }
        if (cmd.startAt() == null || cmd.endAt() == null) {
            throw AppEventPeriodRequiredException.EXCEPTION;
        }
        if (!cmd.startAt().isBefore(cmd.endAt())) {
            throw AppEventInvalidPeriodException.EXCEPTION;
        }
    }
}
