package com.storix.api.domain.works.usecase;

import com.storix.api.domain.works.controller.dto.WorksImportRequest;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse;
import com.storix.domain.domains.works.dto.WorksImportResult;
import com.storix.domain.domains.works.service.WorksImportService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminWorksUseCase {

    private final WorksImportService worksImportService;

    // 작품 enum 카탈로그 조회
    public WorksEnumCatalogResponse getEnumCatalog() {
        return WorksEnumCatalogResponse.create();
    }

    // 검수 통과 작품 적재
    public List<WorksImportResult> importWorks(WorksImportRequest request) {
        return worksImportService.importAll(request.toItems());
    }
}
