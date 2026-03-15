package com.storix.api.domain.image.controller;

import com.storix.domain.domains.image.dto.FileUploadRequest;
import com.storix.domain.domains.image.dto.PresignedUrlResponse;
import com.storix.domain.domains.image.dto.ProfileImageUploadRequest;
import com.storix.api.domain.image.usecase.ImageUseCase;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
@Tag(name = "이미지", description = "이미지 관련 API")
public class ImageController {

    private final ImageUseCase imageUseCase;

    @Operation(summary = "게시물 이미지 업로드", description = "S3 버킷에 게시물 이미지를 업로드할 수 있는 presignedUrl을 발급하는 api 입니다.   \n해당 url로 이미지를 업로드 한 후, url과 함께 반환된 objectKey는 독자/작가 게시물 등록 api로 보내주세요.")
    @PostMapping("/board")
    public ResponseEntity<CustomResponse<List<PresignedUrlResponse>>> getBoardImagePresignedUrl(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody FileUploadRequest req
    ) {
        return ResponseEntity.ok()
                .body(imageUseCase.getBoardImagePresignedUrl(authUserDetails, req));
    }

    @Operation(summary = "프로필 이미지 업로드", description = "S3 버킷에 프로필 이미지를 업로드할 수 있는 presignedUrl을 발급하는 api 입니다.   \n해당 url로 이미지를 업로드 한 후, url과 함께 반환된 objectKey는 프로필 변경 api로 보내주세요.")
    @PostMapping("/profile")
    public ResponseEntity<CustomResponse<PresignedUrlResponse>> getProfileImagePresignedUrl(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Valid @RequestBody ProfileImageUploadRequest req
    ) {
        return ResponseEntity.ok()
                .body(imageUseCase.getProfileImagePresignedUrl(authUserDetails.getUserId(), req));
    }

    @Operation(summary = "[테스트용] 이미지 확인", description = "S3 버킷에 업로드된 이미지를 확인할 수 있는 presignedGetUrl을 발급하는 api 입니다.   \nobjectKey를 보내주세요.")
    @GetMapping("/confirm")
    public ResponseEntity<CustomResponse<String>> getProfileImagePresignedUrl(
            @RequestParam String objectKey
    ) {
        return ResponseEntity.ok()
                .body(imageUseCase.getImageUrl(objectKey));
    }

}
