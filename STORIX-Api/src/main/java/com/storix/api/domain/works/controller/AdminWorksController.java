package com.storix.api.domain.works.controller;

import com.storix.api.domain.works.controller.dto.WorksImportRequest;
import com.storix.api.domain.works.usecase.AdminWorksUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse;
import com.storix.domain.domains.works.dto.WorksImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/works")
@RequiredArgsConstructor
@Tag(name = "관리자 작품", description = "관리자 작품 적재 API")
public class AdminWorksController {

    private final AdminWorksUseCase adminWorksUseCase;

    @GetMapping("/enum-catalog")
    @Operation(summary = "작품 enum 카탈로그", description = "장르 · 연령가 · 작품 유형 · 플랫폼의 enum 이름과 DB 값을 내려줍니다. storedAs 는 DB 에 어느 값이 저장되는지입니다. 크롤러 검수 서비스가 원문을 enum 으로 매핑할 때 씁니다.")
    public CustomResponse<WorksEnumCatalogResponse> getEnumCatalog() {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, adminWorksUseCase.getEnumCatalog());
    }

    @PostMapping("/import")
    @Operation(
            summary = "작품 적재",
            description = "검수를 통과한 작품을 최대 100건씩 적재합니다. 라벨 · 띄어쓰기 · 기호를 뺀 제목과 작품 유형이 같고 작가가 한 명이라도 겹치면 같은 작품으로 보고 갱신합니다. "
                    + "개정판 · 완전판 · 시즌 같은 판본 표기는 다른 작품이고, 외전은 본편과 같은 작품입니다. "
                    + "같은 작품이 없지만 제목이 비슷하고 작가가 겹치는 작품이 있으면 만들지 않고 SUSPECTED_DUPLICATE 와 candidateWorksIds 를 돌려줍니다. "
                    + "같은 웹소설이 있는 단행본은 만들지 않고 SKIPPED 와 그 웹소설 id 를 돌려줍니다. "
                    + "갱신할 때 새 값이 있으면 덮어쓰고 비어 있으면 기존 값을 둡니다. enum 은 카탈로그의 name 으로 보내고 worksType 은 필수입니다. "
                    + "targetWorksId 를 보내면 판정 없이 그 작품의 보낸 필드만 갱신하고, 작품명 · 작가 · 작품 유형을 생략할 수 있습니다. 링크만 채울 때 platform · landingUrl 만 보내면 됩니다. "
                    + "한 건이 실패해도 나머지는 저장되고, 건마다 CREATED · UPDATED · UNCHANGED · SUSPECTED_DUPLICATE · SKIPPED · FAILED 를 돌려줍니다. "
                    + "바뀐 작품은 작품 검색에 바로 반영됩니다. 동시에 두 번 호출하면 409 입니다."
    )
    public CustomResponse<List<WorksImportResult>> importWorks(@Valid @RequestBody WorksImportRequest request) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, adminWorksUseCase.importWorks(request));
    }
}
