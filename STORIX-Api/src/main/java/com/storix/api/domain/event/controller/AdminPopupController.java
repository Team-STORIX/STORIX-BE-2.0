package com.storix.api.domain.event.controller;

import com.storix.api.domain.event.usecase.PopupUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.api.domain.event.controller.dto.PopupRequest;
import com.storix.domain.domains.event.dto.PopupResponse;
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
@RequestMapping("/api/v1/admin/popups")
@RequiredArgsConstructor
@Validated
@Tag(name = "관리자 팝업", description = "관리자 팝업 관리 API")
public class AdminPopupController {

    private final PopupUseCase eventPopupUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이벤트 팝업 생성", description = "multipart 요청: 'data'(application/json) 파트에 팝업 정보, 'file' 파트에 이미지. 서버가 이미지를 S3 업로드 후 생성합니다. 활성 팝업 노출 기간이 겹치면 생성할 수 없습니다.")
    public CustomResponse<PopupResponse> createPopup(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestPart("data") PopupRequest req,
            @RequestPart("file") MultipartFile file
    ) {
        return eventPopupUseCase.createPopup(authUser, req, file);
    }

    @GetMapping
    @Operation(summary = "이벤트 팝업 목록 조회", description = "최신순 번호형 페이지네이션. 페이지당 10개 고정이며 totalPages/totalElements 를 함께 반환합니다. 검색 시 keyword로 팝업명을 보내주세요.")
    public CustomResponse<PageResponseWrapperDTO<PopupResponse>> getPopups(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) String keyword
    ) {
        return eventPopupUseCase.getPopups(page, keyword);
    }

    @GetMapping("/{popupId}")
    @Operation(summary = "이벤트 팝업 단건 조회")
    public CustomResponse<PopupResponse> getPopup(
            @PathVariable Long popupId
    ) {
        return eventPopupUseCase.getPopup(popupId);
    }

    @PutMapping(value = "/{popupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이벤트 팝업 수정", description = "multipart 요청: 'data' 파트에 팝업 정보, 'file' 파트에 새 이미지(선택 - 없으면 기존 이미지 유지). 수정 즉시 앱 active 조회 응답에 반영됩니다.")
    public CustomResponse<PopupResponse> updatePopup(
            @PathVariable Long popupId,
            @Valid @RequestPart("data") PopupRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return eventPopupUseCase.updatePopup(popupId, req, file);
    }

    @PatchMapping("/{popupId}/cancel")
    @Operation(summary = "이벤트 팝업 강제 종료")
    public CustomResponse<PopupResponse> cancelPopup(
            @PathVariable Long popupId
    ) {
        return eventPopupUseCase.cancelPopup(popupId);
    }
}
