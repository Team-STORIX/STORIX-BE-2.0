package com.storix.api.domain.search.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.WorksReindexResponse;
import com.storix.domain.domains.search.service.WorksIndexService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class AdminSearchUseCase {

    private final WorksIndexService worksIndexService;

    // 작품 검색 재색인
    public WorksReindexResponse reindexWorks() {
        return worksIndexService.reindexAll();
    }
}
