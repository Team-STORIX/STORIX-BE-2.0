package com.storix.api.domain.preference;

import com.storix.domain.domains.preference.application.ExplorationUseCase;
import com.storix.domain.domains.preference.dto.ExplorationResultResponseDto;
import com.storix.domain.domains.preference.dto.ExplorationSubmitRequestDto;
import com.storix.domain.domains.preference.dto.ExplorationWorksResponseDto;
import com.storix.domain.domains.preference.dto.GenreScoreInfo;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/preference")
@Tag(name = "취향 분석", description = "취향 분석 API")
public class PreferenceController {

    private final ExplorationUseCase explorationUseCase;

    // 탐색 목록 조회
    @Operation(summary = "취향 탐색용 작품 목록", description = "취향 탐색 시 사용할 작품 리스트를 조회합니다. 유저가 이미 취향 분석을 한 작품은 조회되지 않도록 합니다. " +
            "취향 탐색은 하루 한 번만 가능합니다. 이미 한 경우에 재요청 할 경우 빈 리스트를 반환합니다.")
    @GetMapping("/exploration")
    public CustomResponse<List<ExplorationWorksResponseDto>> getExploration(
            @AuthenticationPrincipal AuthUserDetails authUserDetails) {

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                explorationUseCase.getExplorationWorks(authUserDetails.getUserId())
        );

    }

    // 탐색 제출 (좋아요/별로예요)
    @Operation(summary = "취향 분석", description = "개별 작품에 대한 분석 요청을 진행합니다.")
    @PostMapping("/exploration")
    public CustomResponse<String> submitResponse(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody @Valid ExplorationSubmitRequestDto request
    ) {
        explorationUseCase.submitExploration(authUserDetails.getUserId(), request);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS);
    }

    // 결과 페이지 조회 (오늘 진행한 15개)
    @Operation(summary = "취향 분석 결과", description = "취향 분석 결과에 대해 조회합니다.")
    @GetMapping("/results")
    public CustomResponse<ExplorationResultResponseDto> getResults(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return CustomResponse.onSuccess(SuccessCode.SUCCESS,
                explorationUseCase.getExplorationResults(authUserDetails.getUserId()));
    }

    // 마이페이지 누적 조회
    @Operation(summary = "마이페이지 선호 장르 통계", description = "레이더 차트용 선호 장르별 점수를 조회합니다.")
    @GetMapping("/stats")
    public CustomResponse<List<GenreScoreInfo>> getStats(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                explorationUseCase.getCumulativeStats(authUserDetails.getUserId())
        );
    }
}