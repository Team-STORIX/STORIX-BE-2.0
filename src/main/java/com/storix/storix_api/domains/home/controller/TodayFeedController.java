package com.storix.storix_api.domains.home.controller;

import com.storix.storix_api.domains.home.dto.SlicedReaderBoardWithProfileInfo;
import com.storix.storix_api.domains.home.usecase.TodayFeedUseCase;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "홈", description = "홈화면 관련 API")
public class TodayFeedController {

    private final TodayFeedUseCase todayFeedUseCase;

    @Operation(summary = "오늘의 피드", description = "오늘의 피드를 조회하는 api 입니다.")
    @GetMapping("/feeds/today")
    public ResponseEntity<CustomResponse<List<SlicedReaderBoardWithProfileInfo>>> getAllReaderBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(todayFeedUseCase.getTodayTrendingFeeds(authUserDetails));
    }

}
