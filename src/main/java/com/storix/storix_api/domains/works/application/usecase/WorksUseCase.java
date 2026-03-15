package com.storix.storix_api.domains.works.application.usecase;

import com.storix.storix_api.domains.works.dto.WorksDetailResponseDto;

public interface WorksUseCase {

    WorksDetailResponseDto getWorksDetail(Long userId, Long worksId);
}
