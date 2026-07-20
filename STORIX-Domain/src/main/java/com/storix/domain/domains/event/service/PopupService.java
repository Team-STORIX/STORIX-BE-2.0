package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.PopupAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.domain.Popup;
import com.storix.domain.domains.event.domain.PopupStatus;
import com.storix.domain.domains.event.dto.DisplayPeriod;
import com.storix.domain.domains.event.dto.PopupCommand;
import com.storix.domain.domains.event.dto.PopupResponse;
import com.storix.domain.domains.event.exception.PopupAppEventRequiredException;
import com.storix.domain.domains.event.exception.PopupInvalidDisplayPeriodException;
import com.storix.domain.domains.event.exception.PopupOutOfEventPeriodException;
import com.storix.domain.domains.event.exception.PopupOverlappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PopupService {

    private static final int POPUP_PAGE_SIZE = 10;

    private final PopupAdaptor eventPopupAdaptor;
    private final AppEventAdaptor appEventAdaptor;
    private final EventDisplayPeriodHelper eventDisplayPeriodHelper;

    @Transactional
    public Popup create(PopupCommand cmd, Long adminUserId) {
        validatePeriod(cmd.displayStartAt(), cmd.displayEndAt());
        validateAppEventRequired(cmd.contentTargetType(), cmd.appEventId() != null);
        // appEventId 없으면 독립 팝업, 있으면 이벤트 기간으로 clamp
        AppEvent appEvent = cmd.appEventId() == null ? null : appEventAdaptor.findById(cmd.appEventId());
        DisplayPeriod period = clampToAppEvent(appEvent, cmd.displayStartAt(), cmd.displayEndAt());
        validateNoOverlap(period.start(), period.end(), null);
        return eventPopupAdaptor.save(Popup.builder()
                .appEvent(appEvent)
                .contentTargetType(cmd.contentTargetType())
                .exposurePolicy(cmd.exposurePolicy())
                .popupTitle(cmd.popupTitle())
                .imageObjectKey(cmd.imageObjectKey())
                .content(cmd.content())
                .ctaText(cmd.ctaText())
                .displayStartAt(period.start())
                .displayEndAt(period.end())
                .assigneeAdminId(adminUserId)
                .build());
    }

    @Transactional
    public Popup update(Long popupId, PopupCommand cmd) {
        validatePeriod(cmd.displayStartAt(), cmd.displayEndAt());
        Popup popup = getById(popupId);
        // appEvent 는 불변 - 기존 팝업이 소속된 이벤트 기준으로 검증/clamp
        validateAppEventRequired(cmd.contentTargetType(), popup.getAppEvent() != null);
        DisplayPeriod period = clampToAppEvent(popup.getAppEvent(), cmd.displayStartAt(), cmd.displayEndAt());
        // 종료된 팝업은 다시 노출되지 않으므로 기간 중복 검증 대상에서 제외
        if (popup.getStatus() != PopupStatus.ENDED) {
            validateNoOverlap(period.start(), period.end(), popupId);
        }
        popup.update(
                cmd.contentTargetType(),
                cmd.exposurePolicy(),
                cmd.popupTitle(),
                cmd.imageObjectKey(),
                cmd.content(),
                cmd.ctaText(),
                period.start(),
                period.end()
        );
        return popup;
    }

    @Transactional(readOnly = true)
    public Popup getById(Long popupId) {
        return eventPopupAdaptor.findById(popupId);
    }

    @Transactional(readOnly = true)
    public Page<Popup> getPopups(int page, String keyword) {
        String normalized = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return eventPopupAdaptor.searchByTitle(normalized, PageRequest.of(page, POPUP_PAGE_SIZE));
    }

    // objectKey 형태로 반환, baseUrl/캐시는 UseCase 담당
    @Transactional(readOnly = true)
    public Optional<PopupResponse> findActivePopup(LocalDateTime now) {
        return eventPopupAdaptor.findActivePopup(now).map(PopupResponse::from);
    }

    @Transactional
    public Popup cancel(Long popupId) {
        Popup popup = getById(popupId);
        popup.end();
        return popup;
    }

    @Transactional
    public int activateDuePopups(LocalDateTime now) {
        List<Popup> popups = eventPopupAdaptor.findDueToActivate(now);
        popups.forEach(Popup::activate);
        return popups.size();
    }

    @Transactional
    public int endExpiredPopups(LocalDateTime now) {
        List<Popup> popups = eventPopupAdaptor.findDueToEnd(now);
        popups.forEach(Popup::end);
        return popups.size();
    }

    // AppEvent 종료 cascade: 소속 활성 팝업 일괄 종료
    @Transactional
    public void endByAppEvent(Long appEventId) {
        eventPopupAdaptor.findActiveByAppEvent(appEventId).forEach(Popup::end);
    }

    // AppEvent 기간 변경 cascade: 소속 팝업 노출기간을 이벤트 기간 안으로 clamp
    @Transactional
    public void clampByAppEvent(Long appEventId, LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        eventPopupAdaptor.findActiveByAppEvent(appEventId)
                .forEach(popup -> popup.clampToEventPeriod(eventStartAt, eventEndAt));
    }

    private void validatePeriod(LocalDateTime displayStartAt, LocalDateTime displayEndAt) {
        eventDisplayPeriodHelper.validate(displayStartAt, displayEndAt, () -> PopupInvalidDisplayPeriodException.EXCEPTION);
    }

    private void validateAppEventRequired(ContentTargetType contentTargetType, boolean hasAppEvent) {
        eventDisplayPeriodHelper.requireAppEventForType(contentTargetType, hasAppEvent, () -> PopupAppEventRequiredException.EXCEPTION);
    }

    private DisplayPeriod clampToAppEvent(AppEvent appEvent, LocalDateTime displayStartAt, LocalDateTime displayEndAt) {
        return eventDisplayPeriodHelper.clampToAppEvent(appEvent, displayStartAt, displayEndAt, () -> PopupOutOfEventPeriodException.EXCEPTION);
    }

    private void validateNoOverlap(LocalDateTime displayStartAt, LocalDateTime displayEndAt, Long excludeId) {
        if (eventPopupAdaptor.existsOverlapping(displayStartAt, displayEndAt, excludeId)) {
            throw PopupOverlappingException.EXCEPTION;
        }
    }
}
