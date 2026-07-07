package com.storix.api.domain.event.usecase;

import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.common.utils.STORIXStatic;
import com.storix.api.domain.event.controller.dto.BannerRequest;
import com.storix.api.domain.event.controller.dto.PageResponse;
import com.storix.api.domain.image.helper.S3UploadHelper;
import com.storix.domain.domains.event.domain.Banner;
import com.storix.domain.domains.event.dto.BannerResponse;
import com.storix.domain.domains.event.service.BannerService;
import com.storix.domain.domains.event.service.EventContentCacheHelper;
import com.storix.domain.domains.image.domain.EventImageSurface;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class BannerUseCase {

    private final BannerService eventBannerService;
    private final EventContentCacheHelper eventContentCacheHelper;
    private final S3UploadHelper s3UploadHelper;

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    // 이벤트 배너 생성
    public CustomResponse<BannerResponse> createBanner(AuthUserDetails authUser, BannerRequest req, MultipartFile file) {

        // 1. 이벤트 이미지 S3 업로드
        String imageObjectKey = s3UploadHelper.uploadEventImage(file, req.appEventId(), EventImageSurface.BANNER);

        // 2. 배너 생성. 검증 실패 시 올린 이미지 롤백
        Banner banner;
        try {
            banner = eventBannerService.create(req.toCommand(imageObjectKey), authUser.getUserId());
        } catch (Exception e) {
            s3UploadHelper.delete(imageObjectKey);
            throw e;
        }

        // 3. 이벤트 배너 캐시 무효화
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_BANNER_KEY);
        return CustomResponse.onSuccess(SuccessCode.EVENT_BANNER_CREATE_SUCCESS, BannerResponse.from(banner).withBaseUrl(baseUrl));
    }

    // 이벤트 배너 목록 조회
    public CustomResponse<PageResponse<BannerResponse>> getBanners(int page) {

        PageResponse<BannerResponse> result = PageResponse.from(
                eventBannerService.getBanners(page)
                        .map(banner -> BannerResponse.from(banner).withBaseUrl(baseUrl)));
        return CustomResponse.onSuccess(SuccessCode.EVENT_BANNER_LOAD_SUCCESS, result);
    }

    // 이벤트 배너 단건 조회
    public CustomResponse<BannerResponse> getBanner(Long bannerId) {

        Banner banner = eventBannerService.getById(bannerId);
        return CustomResponse.onSuccess(SuccessCode.EVENT_BANNER_LOAD_SUCCESS, BannerResponse.from(banner).withBaseUrl(baseUrl));
    }

    // 이벤트 배너 수정
    public CustomResponse<BannerResponse> updateBanner(Long bannerId, BannerRequest req, MultipartFile file) {

        // 1. 이미지 파일이 있으면 새로 업로드, 없으면 기존 유지
        boolean replaceImage = file != null && !file.isEmpty();
        String oldImageObjectKey = eventBannerService.getById(bannerId).getImageObjectKey();
        String imageObjectKey = replaceImage
                ? s3UploadHelper.uploadEventImage(file, req.appEventId(), EventImageSurface.BANNER)
                : oldImageObjectKey;

        // 2. 배너 수정. 실패 시 새로 올린 이미지 롤백
        Banner banner;
        try {
            banner = eventBannerService.update(bannerId, req.toCommand(imageObjectKey));
        } catch (Exception e) {
            if (replaceImage) s3UploadHelper.delete(imageObjectKey);
            throw e;
        }

        // 3. 교체 성공 시 옛 이미지 정리 + 캐시 무효화
        if (replaceImage) s3UploadHelper.delete(oldImageObjectKey);
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_BANNER_KEY);
        return CustomResponse.onSuccess(SuccessCode.EVENT_BANNER_UPDATE_SUCCESS, BannerResponse.from(banner).withBaseUrl(baseUrl));
    }

    // 이벤트 배너 강제 종료
    public CustomResponse<BannerResponse> cancelBanner(Long bannerId) {

        // 1. 배너 종료
        Banner banner = eventBannerService.cancel(bannerId);

        // 2. 이벤트 배너 캐시 무효화
        eventContentCacheHelper.evict(STORIXStatic.ACTIVE_BANNER_KEY);
        return CustomResponse.onSuccess(SuccessCode.EVENT_BANNER_CANCEL_SUCCESS, BannerResponse.from(banner).withBaseUrl(baseUrl));
    }
}
