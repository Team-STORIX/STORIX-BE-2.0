package com.storix.api.domain.adultverification.controller;

import com.storix.api.domain.adultverification.controller.dto.AdultVerificationConfirmRequest;
import com.storix.api.domain.adultverification.controller.dto.AdultVerificationStatusResponse;
import com.storix.api.domain.adultverification.controller.dto.AdultVerificationTicketResponse;
import com.storix.api.domain.adultverification.usecase.AdultVerificationUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adult-verifications")
@RequiredArgsConstructor
@Tag(name = "성인인증", description = "포트원 KG이니시스 통합인증 기반 성인인증 API")
public class AdultVerificationController {

    private final AdultVerificationUseCase adultVerificationUseCase;

    @PostMapping
    @Operation(summary = "[1] 본인인증 요청 발급", description = "인증창을 띄우기 직전에 호출해주세요.   \n" +
            "응답으로 받은 identityVerificationId, storeId, channelKey 를 그대로 PortOne.requestIdentityVerification() 에 넘기면 됩니다.   \n" +
            "identityVerificationId 는 앱이 만들지 않고 서버가 발급합니다.   \n\n" +
            "**확정 전까지는 몇 번을 호출해도 같은 티켓을 돌려줍니다.**   \n" +
            "인증창을 닫고 다시 시도하거나 버튼이 연타돼도 인증 건이 새로 생기지 않으니, 재시도 시 그대로 다시 호출하시면 됩니다.   \n" +
            "이미 성인인증이 유효한 유저는 409 로 거부합니다. 인증창을 띄우기 전에 상태 조회로 걸러주세요.")
    public CustomResponse<AdultVerificationTicketResponse> issue(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return adultVerificationUseCase.issue(authUser.getUserId());
    }

    @PostMapping("/confirm")
    @Operation(summary = "[2] 본인인증 확정", description = "인증창이 성공으로 닫힌 뒤 호출해주세요.   \n" +
            "서버가 포트원에 다시 조회해 성인 여부를 판정합니다. 앱이 보낸 성공 여부는 신뢰하지 않습니다.   \n" +
            "**같은 identityVerificationId 로 다시 호출해도 같은 응답을 돌려줍니다.**   \n" +
            "버튼이 연타되거나 응답이 유실돼 재시도해도 인증이 두 번 처리되지 않으니 그대로 다시 호출하시면 됩니다.   \n" +
            "단, 유효기간이 지났거나 해제된 건은 409 로 거부합니다. 발급부터 다시 진행해주세요.   \n" +
            "인증창이 실패나 취소로 닫힌 경우에는 호출하지 않아도 됩니다. 발급을 다시 호출하면 같은 티켓으로 재시도됩니다.")
    public CustomResponse<AdultVerificationStatusResponse> confirm(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @RequestBody @Valid AdultVerificationConfirmRequest request
    ) {
        return adultVerificationUseCase.confirm(authUser.getUserId(), request.identityVerificationId());
    }

    @GetMapping("/me")
    @Operation(summary = "성인인증 상태 조회", description = "현재 로그인한 유저의 성인인증 유효 여부와 만료 시각을 조회합니다.   \n" +
            "유효기간은 1년이며 만료되면 재인증이 필요합니다.")
    public CustomResponse<AdultVerificationStatusResponse> getStatus(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {
        return adultVerificationUseCase.getStatus(authUser.getUserId());
    }

}
