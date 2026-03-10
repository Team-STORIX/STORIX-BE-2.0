package com.storix.domain.domains.works.application.usecase;

import com.storix.domain.domains.works.dto.WorksDetailResponseDto;

public interface WorksUseCase {

    WorksDetailResponseDto getWorksDetail(Long userId, Long worksId);
}
