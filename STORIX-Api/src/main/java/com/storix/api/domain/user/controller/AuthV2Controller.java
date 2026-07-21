package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.LogoutRequest;
import com.storix.api.domain.user.controller.dto.WithdrawRequest;
import com.storix.api.domain.user.usecase.AuthUseCase;
import com.storix.api.domain.user.usecase.LogoutUseCase;
import com.storix.api.domain.user.usecase.WithDrawUseCase;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.OnboardingUserDetails;
import com.storix.api.domain.user.controller.dto.ReaderSignupRequestV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthV2Controller {

    private final AuthUseCase authUseCase;
    private final LogoutUseCase logoutUseCase;
    private final WithDrawUseCase withDrawUseCase;

    @Operation(summary = "[v2] 독자 계정 회원가입", description = "유저 정보를 최종적으로 등록하는 api 입니다.   \n" +
            "- 온보딩 토큰을 헤더로 보내주세요.  \n" +
            "- 필수 동의 3가지를 각각 받습니다: serviceTermsAgree(서비스 이용약관), privacyPolicyAgree(개인정보 수집·이용), ageOver14(만 14세 이상). 모두 true 여야 합니다.  \n" +
            "- 마케팅 알림 수신 동의 여부는 가입 후 별도 모달에서 받습니다.")
    @PostMapping("/users/reader/signup")
    public ResponseEntity<CustomResponse<AuthorizationResponse>> readerUserSignup(
            @AuthenticationPrincipal OnboardingUserDetails onboardingUser,
            @Valid @RequestBody ReaderSignupRequestV2 req
    ) {
        return authUseCase.readerSignup(req.toData(), onboardingUser.getJti());
    }

    @Operation(summary = "[v2] 로그아웃", description = "로그아웃용 api 입니다. 헤더로 액세스 토큰을 보내주세요.  \n" +
            "- Native: RequestBody에 installationId(기기 식별자)를 함께 보내주세요.  \n" +
            "          —> BE에서 해당 디바이스의 FCM 토큰을 즉시 비활성화 합니다.  \n" +
            "- refreshToken을 함께 보내면 해당 기기만 로그아웃합니다.  \n" +
            "          —> 보내지 않으면 모든 기기가 로그아웃됩니다.  \n")
    @PostMapping("/user/logout")
    public ResponseEntity<CustomResponse<Void>> logout(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody LogoutRequest body
    ) {
        String installationId = body != null ? body.installationId() : null;
        String refreshToken = body != null ? body.refreshToken() : null;
        return logoutUseCase.execute(authUserDetails.getUserId(), installationId, refreshToken);
    }

    @Operation(summary = "[v2] 회원 탈퇴", description = "회원 탈퇴용 api 입니다.   \n" +
            "회원 계정과 관련된 Refresh 토큰과 모든 디바이스의 FCM 토큰을 비활성화합니다.   \n" +
            "- 탈퇴 사유(reasons)는 1개 이상 선택해야 하며, reasons 에 OTHER 가 포함된 경우에만 detail(직접 입력, 100자 이내)을 함께 보내주세요.")
    @DeleteMapping("/user/withdraw")
    public ResponseEntity<CustomResponse<Void>> withdraw(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody WithdrawRequest req
    ) {
        return withDrawUseCase.execute(authUserDetails.getUserId(), req.reasons(), req.detail());
    }
}
