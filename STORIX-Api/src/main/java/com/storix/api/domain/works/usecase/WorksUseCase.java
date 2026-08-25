package com.storix.api.domain.works.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.works.dto.WorksDetailResponseDto;
import com.storix.domain.domains.works.service.WorksService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class WorksUseCase {

    private final WorksService worksService;

    public WorksDetailResponseDto getWorksDetail(Long userId, Long worksId) {
        return worksService.getWorksDetail(userId, worksId);
    }
}
