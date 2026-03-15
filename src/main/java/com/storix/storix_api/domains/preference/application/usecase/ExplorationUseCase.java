package com.storix.storix_api.domains.preference.application.usecase;

import com.storix.storix_api.domains.preference.dto.*;
import java.util.List;

public interface ExplorationUseCase {

    // 탐색할 작품 목록 조회 (1일 1회 제한 & 중복 제외)
    List<ExplorationWorksResponseDto> getExplorationWorks(Long userId);

    // 개별 작품 응답 제출
    void submitExploration(Long userId, ExplorationSubmitRequestDto request);

    // 결과 모아보기
    ExplorationResultResponseDto getExplorationResults(Long userId);

    // 마이페이지용 누적 통계
    List<GenreScoreInfo> getCumulativeStats(Long userId);
}