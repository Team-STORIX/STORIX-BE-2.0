package com.storix.api.domain.bannedword.controller;

import com.storix.api.domain.bannedword.controller.dto.BannedWordBulkCreateRequest;
import com.storix.api.domain.bannedword.controller.dto.BannedWordCreateRequest;
import com.storix.api.domain.bannedword.usecase.AdminBannedWordUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.bannedword.dto.BannedWordPageResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/banned-words")
@RequiredArgsConstructor
@Tag(name = "관리자 금칙어", description = "관리자 금칙어 관리 API")
public class AdminBannedWordController {

    private final AdminBannedWordUseCase adminBannedWordUseCase;

    @GetMapping
    @Operation(summary = "금칙어 목록 조회", description = "등록된 금칙어를 페이지 단위로 조회합니다. keyword로 부분 검색이 가능합니다.")
    public CustomResponse<BannedWordPageResponse> searchBannedWords(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "검색할 금칙어 키워드") @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        BannedWordPageResponse response = adminBannedWordUseCase.searchBannedWords(keyword, pageable);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, response);
    }

    @PostMapping
    @Operation(summary = "금칙어 단건 등록", description = "금칙어를 한 건 등록하고 캐시를 즉시 갱신합니다.")
    public CustomResponse<Void> addBannedWord(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody BannedWordCreateRequest request
    ) {
        adminBannedWordUseCase.addBannedWord(request);
        return CustomResponse.onSuccess(SuccessCode.CREATED, null);
    }

    @PostMapping("/bulk")
    @Operation(summary = "금칙어 벌크 등록", description = "여러 금칙어를 한 번에 등록하고 캐시를 즉시 갱신합니다. 이미 등록된 단어는 건너뜁니다.")
    public CustomResponse<Void> addBannedWords(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody BannedWordBulkCreateRequest request
    ) {
        adminBannedWordUseCase.addBannedWords(request);
        return CustomResponse.onSuccess(SuccessCode.CREATED, null);
    }

    @PostMapping(value = "/bulk/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "금칙어 CSV 벌크 등록",
            description = "CSV 파일을 업로드해 금칙어를 일괄 등록합니다. 각 줄의 첫 번째 컬럼을 단어로 읽으며, 이미 등록된 단어는 건너뛰고 캐시를 즉시 갱신합니다."
    )
    public CustomResponse<Void> addBannedWordsFromCsv(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "금칙어 목록이 담긴 CSV 파일") @RequestParam("file") MultipartFile file
    ) {
        adminBannedWordUseCase.addBannedWordsFromCsv(file);
        return CustomResponse.onSuccess(SuccessCode.CREATED, null);
    }

    @DeleteMapping("/{bannedWordId}")
    @Operation(summary = "금칙어 삭제", description = "금칙어를 삭제하고 캐시를 즉시 갱신합니다.")
    public CustomResponse<Void> deleteBannedWord(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "삭제할 금칙어 ID") @PathVariable Long bannedWordId
    ) {
        adminBannedWordUseCase.deleteBannedWord(bannedWordId);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }

    @PostMapping("/reload")
    @Operation(summary = "금칙어 캐시 수동 갱신", description = "DB에 직접 반영된 변경 등으로 어긋난 캐시를 수동으로 다시 불러옵니다.")
    public CustomResponse<Void> reloadCache(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        adminBannedWordUseCase.reloadCache();
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, null);
    }
}
