package com.storix.api.domain.search.controller;

import com.storix.api.domain.search.usecase.AdminSearchUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.search.dto.WorksReindexResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
@Tag(name = "관리자 검색", description = "관리자 검색 색인 관리 API")
public class AdminSearchController {

    private final AdminSearchUseCase adminSearchUseCase;

    @PostMapping("/works/reindex")
    @Operation(summary = "작품 검색 재색인", description = "작품 전체를 새 인덱스에 다시 색인하고 검색 대상을 교체합니다. 크롤링 작품을 적재한 뒤 호출합니다.")
    public CustomResponse<WorksReindexResponse> reindexWorks() {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, adminSearchUseCase.reindexWorks());
    }
}
