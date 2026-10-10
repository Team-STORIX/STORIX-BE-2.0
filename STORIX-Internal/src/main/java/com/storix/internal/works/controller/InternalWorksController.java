package com.storix.internal.works.controller;

import com.storix.internal.works.controller.dto.HashtagRemoveRequest;
import com.storix.internal.works.controller.dto.WorksHashtagRemoveRequest;
import com.storix.internal.works.controller.dto.WorksImportRequest;
import com.storix.internal.works.controller.dto.WorksMergeRequest;
import com.storix.internal.works.controller.dto.WorksRenameRequest;
import com.storix.internal.works.usecase.InternalWorksUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.works.dto.HashtagRemoveResult;
import com.storix.domain.domains.works.dto.WorksArtistInfoPage;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse;
import com.storix.domain.domains.works.dto.WorksHashtagRemoveResult;
import com.storix.domain.domains.works.dto.WorksImportResult;
import com.storix.domain.domains.works.dto.WorksMergeResult;
import com.storix.domain.domains.works.dto.WorksRenameResult;
import com.storix.common.utils.STORIXStatic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/works")
@RequiredArgsConstructor
@Tag(name = "내부 작품", description = "크롤러 등 내부 서비스 전용 작품 API. X-Internal-Api-Key 헤더로만 호출합니다")
@SecurityRequirement(name = STORIXStatic.Internal.SWAGGER_SCHEME)
public class InternalWorksController {

    private final InternalWorksUseCase internalWorksUseCase;

    @GetMapping("/enum-catalog")
    @Operation(summary = "작품 enum 카탈로그", description = "장르 · 연령가 · 작품 유형 · 플랫폼의 enum 이름과 DB 값을 내려줍니다. storedAs 는 DB 에 어느 값이 저장되는지입니다. 크롤러 검수 서비스가 원문을 enum 으로 매핑할 때 씁니다.")
    public CustomResponse<WorksEnumCatalogResponse> getEnumCatalog() {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, internalWorksUseCase.getEnumCatalog());
    }

    @GetMapping("/artists")
    @Operation(
            summary = "작품 작가 필드 목록",
            description = "작가 표기 보정용으로 작품마다 worksType · originalAuthor · author · illustrator 원본과 저장된 artistName 을 worksId 순으로 내려줍니다. "
                    + "afterWorksId 보다 큰 작품부터 size 개(최대 1000)를 주고, 다음 요청에 쓸 nextAfterWorksId 를 돌려줍니다. 마지막이면 null 입니다."
    )
    public CustomResponse<WorksArtistInfoPage> getArtistInfos(
            @RequestParam(defaultValue = "0") Long afterWorksId,
            @RequestParam(defaultValue = "500") int size
    ) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, internalWorksUseCase.getArtistInfos(afterWorksId, size));
    }

    @PostMapping("/import")
    @Operation(
            summary = "작품 적재",
            description = "검수를 통과한 작품을 최대 100건씩 적재합니다. 라벨 · 띄어쓰기 · 기호를 뺀 제목과 작품 유형이 같고 작가가 한 명이라도 겹치면 같은 작품으로 보고 갱신합니다. "
                    + "개정판 · 완전판 · 시즌 같은 판본 표기는 다른 작품이고, 외전은 본편과 같은 작품입니다. "
                    + "같은 작품이 없지만 제목이 비슷하고 작가가 겹치는 작품이 있으면 만들지 않고 SUSPECTED_DUPLICATE 와 candidateWorksIds 를 돌려줍니다. "
                    + "같은 웹소설이 있는 단행본은 만들지 않고 SKIPPED 와 그 웹소설 id 를 돌려줍니다. "
                    + "갱신할 때 새 값이 있으면 덮어쓰고 비어 있으면 기존 값을 둡니다. 연령은 기존보다 높을 때만 바꿉니다. 해시태그는 기존에 더하기만 합니다. enum 은 카탈로그의 name 으로 보내고 worksType 은 필수입니다. "
                    + "targetWorksId 를 보내면 판정 없이 그 작품의 보낸 필드만 갱신하고, 작품명 · 작가 · 작품 유형을 생략할 수 있습니다. 링크만 채울 때 platform · landingUrl 만 보내면 됩니다. "
                    + "한 건이 실패해도 나머지는 저장되고, 건마다 CREATED · UPDATED · UNCHANGED · SUSPECTED_DUPLICATE · SKIPPED · FAILED 를 돌려줍니다. "
                    + "바뀐 작품은 작품 검색에 바로 반영됩니다. 동시에 두 번 호출하면 409 입니다."
    )
    public CustomResponse<List<WorksImportResult>> importWorks(@Valid @RequestBody WorksImportRequest request) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, internalWorksUseCase.importWorks(request));
    }

    @PostMapping("/{keepWorksId}/merge")
    @Operation(
            summary = "작품 병합",
            description = "중복 작품(drop)을 keep 작품으로 합치고 drop 을 지웁니다. 한 묶음이 트랜잭션 하나라 거절 조건이 하나라도 있으면 전부 되돌립니다. "
                    + "작품 정보는 keep 값을 우선하고 비어 있는 필드만 drop 값으로 채웁니다. 연령은 둘 중 높은 값입니다. 플랫폼 · 해시태그 · 별칭은 합칩니다. "
                    + "관심작품 · 장르 점수 기록 · 취향 탐색은 옮기고, 같은 사용자 기록이 keep 에 이미 있으면 drop 것을 지웁니다. 피드 글 · 리뷰 · 토픽룸은 옮깁니다. "
                    + "같은 사용자 리뷰가 양쪽에 있거나 keep 과 drop 모두 토픽룸이 있으면 409(WORKS_ERROR_011)로 거절하고 이유에 어느 drop 의 어느 테이블인지 적습니다. "
                    + "옮긴 뒤 keep 의 리뷰 수 · 평균 별점을 다시 계산하고, drop 이 온보딩 작품이면 keep 을 온보딩 작품으로 올립니다. "
                    + "작품 검색에서 drop 은 빠지고 keep 은 다시 색인됩니다. 작품 적재와 동시에 돌 수 없고 겹치면 409(WORKS_ERROR_007)입니다."
    )
    public CustomResponse<WorksMergeResult> mergeWorks(
            @PathVariable Long keepWorksId,
            @Valid @RequestBody WorksMergeRequest request
    ) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, internalWorksUseCase.mergeWorks(keepWorksId, request));
    }

    @PatchMapping("/{worksId}/name")
    @Operation(
            summary = "작품명 변경",
            description = "작품명을 바꿉니다. 적재는 작품명을 덮어쓰지 않아 잘못 들어간 이름을 정정할 때 씁니다. "
                    + "currentName 이 지금 저장된 이름과 다르면 409(WORKS_ERROR_012)로 거절하고 이유에 현재 이름을 적습니다. "
                    + "같은 작품 판정용 정규화 이름도 같이 바뀌고 작품 검색에 바로 반영됩니다. 작품 적재 · 병합과 동시에 돌 수 없고 겹치면 409(WORKS_ERROR_007)입니다."
    )
    public CustomResponse<WorksRenameResult> renameWorks(
            @PathVariable Long worksId,
            @Valid @RequestBody WorksRenameRequest request
    ) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, internalWorksUseCase.renameWorks(worksId, request));
    }

    @PostMapping("/{worksId}/hashtags/remove")
    @Operation(
            summary = "작품 해시태그 제거",
            description = "작품 하나에서 이름이 같은 해시태그 연결만 끊습니다. 해시태그 자체는 남습니다. 적재는 해시태그를 더하기만 해서 잘못 붙은 태그를 뺄 때 씁니다. "
                    + "removed 는 끊은 이름, notFound 는 원래 붙어 있지 않던 이름입니다. 끊은 내역은 로그에 남습니다. "
                    + "작품 적재 · 병합과 동시에 돌 수 없고 겹치면 409(WORKS_ERROR_007)입니다."
    )
    public CustomResponse<WorksHashtagRemoveResult> removeWorksHashtags(
            @PathVariable Long worksId,
            @Valid @RequestBody WorksHashtagRemoveRequest request
    ) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, internalWorksUseCase.removeWorksHashtags(worksId, request));
    }

    @PostMapping("/hashtags/remove")
    @Operation(
            summary = "해시태그 일괄 제거",
            description = "이름이 같은 해시태그를 모든 작품에서 끊습니다. 해시태그 자체는 남습니다. "
                    + "dryRun 이 true 면 끊지 않고 이름마다 붙어 있는 작품 id 와 수만 돌려줍니다. false 면 끊고 같은 모양으로 돌려줍니다. "
                    + "한 요청이 트랜잭션 하나라 중간에 실패하면 전부 되돌립니다. 끊은 내역은 해시태그마다 로그에 남습니다. "
                    + "dryRun 이 false 면 작품 적재 · 병합과 동시에 돌 수 없고 겹치면 409(WORKS_ERROR_007)입니다."
    )
    public CustomResponse<HashtagRemoveResult> removeHashtagsFromAllWorks(@Valid @RequestBody HashtagRemoveRequest request) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, internalWorksUseCase.removeHashtagsFromAllWorks(request));
    }
}
