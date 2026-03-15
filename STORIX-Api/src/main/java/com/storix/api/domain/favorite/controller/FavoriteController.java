package com.storix.api.domain.favorite.controller;

import com.storix.api.domain.favorite.usecase.FavoriteUseCase;
import com.storix.domain.domains.favorite.dto.FavoriteWorksStatusResponse;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorite")
@RequiredArgsConstructor
@Tag(name = "관심", description = "관심 관련 API")
public class FavoriteController {

    private final FavoriteUseCase favoriteUseCase;

    @Operation(summary = "관심 작품 등록 여부 조회", description = "관심 작품 등록 여부를 조회하는 api 입니다.")
    @GetMapping("/works/{worksId}")
    public ResponseEntity<CustomResponse<FavoriteWorksStatusResponse>> isFavoriteWorks(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long worksId
    ) {
        return ResponseEntity.ok(favoriteUseCase.getFavoriteWorksStatus(authUserDetails, worksId));
    }

    @Operation(summary = "관심 작품 등록", description = "관심 작품을 등록하는 api 입니다.")
    @PostMapping("/works/{worksId}")
    public ResponseEntity<CustomResponse<FavoriteWorksStatusResponse>> addFavoriteWorks(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long worksId
    ) {
        return ResponseEntity.ok(favoriteUseCase.addWorksToFavorite(authUserDetails.getUserId(), worksId));
    }

    @Operation(summary = "관심 작품 해제", description = "관심 작품 등록을 해제하는 api 입니다.")
    @DeleteMapping("/works/{worksId}")
    public ResponseEntity<CustomResponse<FavoriteWorksStatusResponse>> deleteFavoriteWorks(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable Long worksId
    ) {
        return ResponseEntity.ok(favoriteUseCase.deleteFavoriteWorks(authUserDetails.getUserId(), worksId));
    }

}
