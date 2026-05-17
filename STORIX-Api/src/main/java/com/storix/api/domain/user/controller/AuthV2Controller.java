package com.storix.api.domain.user.controller;

import com.storix.api.domain.user.controller.dto.AuthorizationResponse;
import com.storix.api.domain.user.controller.dto.LogoutRequest;
import com.storix.api.domain.user.usecase.AuthUseCase;
import com.storix.api.domain.user.usecase.LogoutUseCase;
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

    @Operation(summary = "[v2] 독자 계정 회원가입", description = "유저 정보를 최종적으로 등록하는 api 입니다.   \n" +
            "- 온보딩 토큰을 헤더로 보내주세요.  \n" +
            "- v1 의 marketingAgree 필드 대신 termsAgree(서비스 이용약관 동의) 필드를 받습니다.  \n" +
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
            "- Web   : RequestBody 생략하고 요청")
    @PostMapping("/user/logout")
    public ResponseEntity<CustomResponse<Void>> logout(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody(required = false) LogoutRequest body
    ) {
        String installationId = body != null ? body.installationId() : null;
        return logoutUseCase.execute(authUserDetails.getUserId(), installationId);
    }
}
