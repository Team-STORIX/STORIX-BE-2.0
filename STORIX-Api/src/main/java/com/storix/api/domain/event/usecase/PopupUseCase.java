package com.storix.api.domain.event.usecase;

import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.common.utils.STORIXStatic;
import com.storix.api.domain.event.controller.dto.PageResponse;
import com.storix.api.domain.event.controller.dto.PopupRequest;
import com.storix.api.domain.image.helper.S3UploadHelper;
import com.storix.domain.domains.event.domain.Popup;
import com.storix.domain.domains.event.dto.PopupResponse;
import com.storix.domain.domains.event.service.EventContentCacheHelper;
import com.storix.domain.domains.event.service.PopupService;
import com.storix.domain.domains.image.domain.EventImageSurface;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class PopupUseCase {

    private final PopupService eventPopupService;
    private final EventContentCacheHelper eventContentCacheHelper;
    private final S3UploadHelper s3UploadHelper;

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    // 이벤트 팝업 생성
    public CustomResponse<PopupResponse> createPopup(AuthUserDetails authUser, PopupRequest req, MultipartFile file) {

        // 1. 이벤트 이미지 S3 업로드
        String imageObjectKey = s3UploadHelper.uploadEventImage(file, req.appEventId(), EventImageSurface.POPUP);

        // 2. 팝업 생성. 검증 실패 시 올린 이미지 롤백
        Popup popup;
        try {
            popup = eventPopupService.create(req.toCommand(imageObjectKey), authUser.getUserId());
        } catch (Exception e) {
            s3UploadHelper.delete(imageObjectKey);
            throw e;
        }

        // 3. 이벤트 팝업 캐시 무효화
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_POPUP_KEY);
        return CustomResponse.onSuccess(SuccessCode.EVENT_POPUP_CREATE_SUCCESS, PopupResponse.from(popup).withBaseUrl(baseUrl));
    }

    // 이벤트 팝업 목록 조회
    public CustomResponse<PageResponse<PopupResponse>> getPopups(int page) {

        PageResponse<PopupResponse> result = PageResponse.from(
                eventPopupService.getPopups(page)
                        .map(popup -> PopupResponse.from(popup).withBaseUrl(baseUrl)));
        return CustomResponse.onSuccess(SuccessCode.EVENT_POPUP_LOAD_SUCCESS, result);
    }

    // 이벤트 팝업 단건 조회
    public CustomResponse<PopupResponse> getPopup(Long popupId) {

        Popup popup = eventPopupService.getById(popupId);
        return CustomResponse.onSuccess(SuccessCode.EVENT_POPUP_LOAD_SUCCESS, PopupResponse.from(popup).withBaseUrl(baseUrl));
    }

    // 이벤트 팝업 수정
    public CustomResponse<PopupResponse> updatePopup(Long popupId, PopupRequest req, MultipartFile file) {

        // 1. 이미지 파일이 있으면 새로 업로드, 없으면 기존 유지
        boolean replaceImage = file != null && !file.isEmpty();
        String oldImageObjectKey = eventPopupService.getById(popupId).getImageObjectKey();
        String imageObjectKey = replaceImage
                ? s3UploadHelper.uploadEventImage(file, req.appEventId(), EventImageSurface.POPUP)
                : oldImageObjectKey;

        // 2. 팝업 수정. 실패 시 새로 올린 이미지 롤백
        Popup popup;
        try {
            popup = eventPopupService.update(popupId, req.toCommand(imageObjectKey));
        } catch (Exception e) {
            if (replaceImage) s3UploadHelper.delete(imageObjectKey);
            throw e;
        }

        // 3. 교체 성공 시 옛 이미지 정리 + 캐시 무효화
        if (replaceImage) s3UploadHelper.delete(oldImageObjectKey);
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_POPUP_KEY);
        return CustomResponse.onSuccess(SuccessCode.EVENT_POPUP_UPDATE_SUCCESS, PopupResponse.from(popup).withBaseUrl(baseUrl));
    }

    // 이벤트 팝업 강제 종료
    public CustomResponse<PopupResponse> cancelPopup(Long popupId) {

        // 1. 팝업 종료
        Popup popup = eventPopupService.cancel(popupId);

        // 2. 이벤트 팝업 캐시 무효화
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_POPUP_KEY);
        return CustomResponse.onSuccess(SuccessCode.EVENT_POPUP_CANCEL_SUCCESS, PopupResponse.from(popup).withBaseUrl(baseUrl));
    }
}
