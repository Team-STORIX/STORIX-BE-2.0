package com.storix.storix_api.domains.hashtag.controller;

import com.storix.storix_api.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.storix_api.domains.hashtag.service.HashtagRecommendService;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hashtags")
@RequiredArgsConstructor
@Tag(name = "홈", description = "홈화면 관련 API")
public class HashtagRestController {

    private final HashtagRecommendService hashtagRecommendationService;

    @Operation(summary = "사용자 맞춤 해시태그 추천", description = "로그인 시 선호 장르 기반, 비로그인 시 전체 인기순으로 해시태그를 추천합니다.")
    @GetMapping("/recommendations")
    public CustomResponse<List<HashtagRecommendResponseDto>> getRecommendedHashtags(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
            ) {

        Long userId = (authUserDetails != null) ? authUserDetails.getUserId() : null;

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                hashtagRecommendationService.getRecommendedHashtags(userId)
        );
    }

}
