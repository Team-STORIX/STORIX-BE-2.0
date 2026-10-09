package com.storix.api.domain.search.controller;

import com.storix.api.domain.search.controller.dto.WorksNicknameCreateRequest;
import com.storix.api.domain.search.usecase.AdminSearchUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.search.dto.WorksNicknameBulkResponse;
import com.storix.domain.domains.search.dto.WorksNicknameResponse;
import com.storix.domain.domains.search.dto.WorksReindexResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/works/{worksId}/nicknames")
    @Operation(summary = "작품 별칭 목록 조회", description = "작품에 등록된 검색용 별칭을 등록순으로 조회합니다.")
    public CustomResponse<List<WorksNicknameResponse>> findWorksNicknames(
            @Parameter(description = "작품 ID") @PathVariable Long worksId
    ) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, adminSearchUseCase.findWorksNicknames(worksId));
    }

    @PostMapping("/works/{worksId}/nicknames")
    @Operation(summary = "작품 별칭 등록", description = "줄임말 등 검색용 별칭을 등록하고 검색에 바로 반영합니다. 띄어쓰기·특수문자만 다른 별칭은 중복으로 봅니다. 재색인 중에는 등록할 수 없습니다.")
    public CustomResponse<WorksNicknameResponse> addWorksNickname(
            @Parameter(description = "작품 ID") @PathVariable Long worksId,
            @Valid @RequestBody WorksNicknameCreateRequest request
    ) {
        return CustomResponse.onSuccess(SuccessCode.CREATED, adminSearchUseCase.addWorksNickname(worksId, request));
    }

    @PostMapping(value = "/works/nicknames/bulk/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "작품 별칭 CSV 일괄 등록",
            description = "works_id,nickname[,works_name] 형식의 CSV 로 별칭을 일괄 등록하고 전체 재색인합니다. "
                    + "첫 줄이 works_id 로 시작하면 헤더로 보고 건너뛰며, 세 번째 칸부터는 검수용이라 읽지 않습니다. "
                    + "이미 등록된 별칭·없는 작품·잘못된 줄은 건너뛰고 건수만 돌려줍니다. "
                    + "재색인이 실패해도 별칭은 저장된 상태이니 재색인 API 를 다시 호출하면 됩니다."
    )
    public CustomResponse<WorksNicknameBulkResponse> addWorksNicknamesFromCsv(
            @Parameter(description = "별칭 목록이 담긴 CSV 파일") @RequestParam("file") MultipartFile file
    ) {
        return CustomResponse.onSuccess(SuccessCode.CREATED, adminSearchUseCase.addWorksNicknamesFromCsv(file));
    }

    @DeleteMapping("/works/{worksId}/nicknames/{nicknameId}")
    @Operation(summary = "작품 별칭 삭제", description = "작품 별칭을 삭제하고 검색에 바로 반영합니다. 재색인 중에는 삭제할 수 없습니다.")
    public CustomResponse<Void> deleteWorksNickname(
            @Parameter(description = "작품 ID") @PathVariable Long worksId,
            @Parameter(description = "삭제할 별칭 ID") @PathVariable Long nicknameId
    ) {
        adminSearchUseCase.deleteWorksNickname(worksId, nicknameId);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }
}
