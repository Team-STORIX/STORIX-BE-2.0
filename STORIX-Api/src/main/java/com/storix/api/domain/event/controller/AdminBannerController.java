package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.controller.dto.BannerRequest;
import com.storix.api.domain.event.usecase.BannerUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.event.dto.BannerResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import com.storix.common.payload.PageResponseWrapperDTO;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
@Validated
@Tag(name = "관리자 배너", description = "관리자 배너 관리 API")
public class AdminBannerController {

    private final BannerUseCase eventBannerUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이벤트 배너 생성", description = "multipart 요청: 'data'(application/json) 파트에 배너 정보, 'file' 파트에 이미지. 서버가 이미지를 S3 업로드 후 생성합니다. 활성 배너 노출 기간이 겹치면 생성할 수 없습니다.")
    public CustomResponse<BannerResponse> createBanner(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestPart("data") BannerRequest req,
            @RequestPart("file") MultipartFile file
    ) {
        return eventBannerUseCase.createBanner(authUser, req, file);
    }

    @GetMapping
    @Operation(summary = "이벤트 배너 목록 조회", description = "최신순 번호형 페이지네이션. 페이지당 10개 고정이며 totalPages/totalElements 를 함께 반환합니다. 검색 시 keyword로 배너명을 보내주세요.")
    public CustomResponse<PageResponseWrapperDTO<BannerResponse>> getBanners(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) String keyword
    ) {
        return eventBannerUseCase.getBanners(page, keyword);
    }

    @GetMapping("/{bannerId}")
    @Operation(summary = "이벤트 배너 단건 조회")
    public CustomResponse<BannerResponse> getBanner(
            @PathVariable Long bannerId
    ) {
        return eventBannerUseCase.getBanner(bannerId);
    }

    @PutMapping(value = "/{bannerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이벤트 배너 수정", description = "multipart 요청: 'data' 파트에 배너 정보, 'file' 파트에 새 이미지(선택). 수정 즉시 앱 active 조회 응답에 반영됩니다.")
    public CustomResponse<BannerResponse> updateBanner(
            @PathVariable Long bannerId,
            @Valid @RequestPart("data") BannerRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return eventBannerUseCase.updateBanner(bannerId, req, file);
    }

    @PatchMapping("/{bannerId}/cancel")
    @Operation(summary = "이벤트 배너 강제 종료")
    public CustomResponse<BannerResponse> cancelBanner(
            @PathVariable Long bannerId
    ) {
        return eventBannerUseCase.cancelBanner(bannerId);
    }
}
