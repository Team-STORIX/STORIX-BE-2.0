package com.storix.storix_api.domains.user.controller;

import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.adaptor.OnboardingUserDetails;
import com.storix.storix_api.domains.user.application.usecase.*;
import com.storix.storix_api.domains.user.controller.dto.*;
import com.storix.storix_api.domains.user.domain.OAuthProvider;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "온보딩", description = "온보딩 관련 API")
public class AuthController {

    private final AuthUseCase authUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final OAuthLoginUseCase oauthLoginUseCase;
    private final AuthorizationUseCase authorizationUseCase;
    private final WithDrawUseCase withDrawUseCase;

    @Operation(summary = "카카오 로그인", description = "카카오로 로그인 하는 api 입니다.   \n회원가입한 유저의 경우 readerLoginResponse로 액세스 토큰을 리프레쉬 토큰 쿠키와 함께 반환합니다.   \n회원가입이 필요한 유저의 경우 readerPreLoginResponse로 온보딩 토큰을 반환합니다.")
    @GetMapping("/oauth/kakao/login")
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> kakaoLogin(
            @RequestParam("code") String code,
            @RequestParam("redirectUri") String redirectUri
    ) {
        OAuthAuthorizationRequest req = OAuthAuthorizationRequest.forKakao(code, redirectUri);
        return oauthLoginUseCase.readerOAuthLogin(req, OAuthProvider.KAKAO);
    }

    @Operation(summary = "네이버 로그인", description = "네이버로 로그인 하는 api 입니다.  \n회원가입한 유저의 경우 readerLoginResponse로 액세스 토큰을 리프레쉬 토큰 쿠키와 함께 반환합니다.   \n회원가입이 필요한 유저의 경우 readerPreLoginResponse로 온보딩 토큰을 반환합니다.")
    @GetMapping("/oauth/naver/login")
    public ResponseEntity<CustomResponse<ReaderSocialLoginResponse>> naverLogin(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        OAuthAuthorizationRequest req = OAuthAuthorizationRequest.forNaver(code, state);
        return oauthLoginUseCase.readerOAuthLogin(req, OAuthProvider.NAVER);
    }

    @Operation(summary = "독자 계정 회원가입", description = "유저 정보를 최종적으로 등록하는 api 입니다.  \n온보딩 토큰을 보내주세요.")
    @PostMapping("/users/reader/signup")
    public ResponseEntity<CustomResponse<AuthorizationResponse>> readerUserSignup(
            @AuthenticationPrincipal OnboardingUserDetails onboardingUser,
            @Valid @RequestBody ReaderSignupRequest req
    ) {
        return authUseCase.readerSignup(req, onboardingUser.getJti());
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임 중복 여부를 체크하는 api 입니다.")
    @GetMapping("/nickname/valid")
    public ResponseEntity<CustomResponse<Void>> nickNameCheck(
            @RequestParam("nickname")
            @NotBlank(message = "닉네임은 필수입니다.")
            @Size(min = 2, max = 10, message = "닉네임은 2~10자까지 가능합니다.")
            @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ ]+$",
                    message = "닉네임은 한글, 영문, 숫자, 공백만 가능하며 자음/모음/공백만으로는 불가능합니다."
            )
            String nickName
    ) {
        return ResponseEntity.ok()
                .body(authUseCase.checkAvailableNickname(nickName));
    }

    @Operation(summary = "작가 계정 일반 로그인", description = "작가 계정에 로그인 하는 api 입니다.")
    @PostMapping("/users/artist/login")
    public ResponseEntity<CustomResponse<AuthorizationResponse>> artistUserLogin(
            @Valid @RequestBody ArtistLoginRequest req
    ) {
        return loginUseCase.artistLoginWithLoginId(req);
    }

    @Operation(summary = "[백엔드용] 작가 계정 회원가입", description = "백엔드용 작가 계정 생성 api 입니다.")
    @PostMapping("/developer/users/artist/signup")
    public ResponseEntity<CustomResponse<ArtistSignupResponse>> developerArtistUserSignup(
            @RequestBody ArtistSignupRequest req
    ) {
        return ResponseEntity.ok()
                .body(authUseCase.artistSignup(req));
    }

    @Operation(summary = "토큰 재발급", description = "액세스 토큰을 리프레쉬 토큰 쿠키와 함께 재발급하는 api 입니다.   \n액세스 토큰 만료 시 호출해주세요.")
    @PostMapping("/tokens/refresh")
    public ResponseEntity<CustomResponse<AuthorizationResponse>> reissueAccessToken(
            @Parameter(hidden = true)
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        return authorizationUseCase.getTokenRefresh(refreshToken);
    }

    @Operation(summary = "로그아웃", description = "로그아웃용 api 입니다.   \n액세스 토큰을 보내주세요.")
    @PostMapping("/user/logout")
    public ResponseEntity<CustomResponse<Void>> logout(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return logoutUseCase.execute(authUserDetails.getUserId());
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴용 api 입니다.   \n액세스 토큰을 보내주세요.")
    @DeleteMapping("/user/withdraw")
    public ResponseEntity<CustomResponse<Void>> withdraw(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return withDrawUseCase.execute(authUserDetails.getUserId());
    }

}
