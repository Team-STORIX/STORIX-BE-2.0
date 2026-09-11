package com.storix.api.domain.preference.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.preference.dto.ExplorationResultResponseDto;
import com.storix.domain.domains.preference.dto.ExplorationSubmitRequestDto;
import com.storix.domain.domains.preference.dto.ExplorationWorksResponseDto;
import com.storix.domain.domains.preference.service.ExplorationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ExplorationUseCase {

    private final ExplorationService explorationService;

    // 탐색할 작품 목록 조회 (1일 1회 제한 & 중복 제외)
    public List<ExplorationWorksResponseDto> getExplorationWorks(Long userId) {
        return explorationService.getExplorationWorks(userId);
    }

    // 개별 작품 응답 제출
    public void submitExploration(Long userId, ExplorationSubmitRequestDto request) {
        explorationService.submitExploration(userId, request);
    }

    // 결과 모아보기
    public ExplorationResultResponseDto getExplorationResults(Long userId) {
        return explorationService.getExplorationResults(userId);
    }
}
