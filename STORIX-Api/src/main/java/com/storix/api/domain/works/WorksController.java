package com.storix.api.domain.works;

import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.works.application.usecase.WorksUseCase;
import com.storix.domain.domains.works.dto.WorksDetailResponseDto;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/works")
@RequiredArgsConstructor
@Tag(name = "작품", description = "작품 관련 REST API")
public class WorksController {

    private final WorksUseCase worksUseCase;

    @Operation(summary = "작품 상세 조회", description = "작품 id로 상세 정보를 조회합니다. 18세 이용가 작품은 로그인 및 성인 인증이 필요합니다.")
    @GetMapping("/{worksId}")
    public CustomResponse<WorksDetailResponseDto> getWorksDetail(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long worksId
    ) {
        Long userId = (authUserDetails != null) ? authUserDetails.getUserId() : null;

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                worksUseCase.getWorksDetail(userId, worksId)
        );
    }
}
