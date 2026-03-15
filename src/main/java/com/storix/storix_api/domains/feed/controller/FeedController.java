package com.storix.storix_api.domains.feed.controller;

import com.storix.storix_api.domains.feed.controller.dto.FeedReportRequest;
import com.storix.storix_api.domains.feed.controller.dto.ReaderBoardReplyRequest;
import com.storix.storix_api.domains.feed.controller.dto.ReaderBoardReplyResponse;
import com.storix.storix_api.domains.feed.domain.FeedSortType;
import com.storix.storix_api.domains.feed.domain.ReplySortType;
import com.storix.storix_api.domains.feed.dto.BoardWrapperDto;
import com.storix.storix_api.domains.feed.dto.LikeToggleResponse;
import com.storix.storix_api.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.storix_api.domains.feed.usecase.FeedKebabUseCase;
import com.storix.storix_api.domains.feed.usecase.FeedReactionUseCase;
import com.storix.storix_api.domains.feed.usecase.FeedUseCase;
import com.storix.storix_api.domains.profile.dto.ProfileSortType;
import com.storix.storix_api.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.works.dto.SlicedWorksInfo;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "피드", description = "피드 관련 API")
public class FeedController {

    private final FeedUseCase feedUseCase;
    private final FeedReactionUseCase feedReactionUseCase;
    private final FeedKebabUseCase feedKebabUseCase;

    @Operation(summary = "[관심 작품] 전체 게시물 확인", description = "전체 게시물을 확인하는 api 입니다. 무한 스크롤로 구성됩니다.")
    @GetMapping("/reader/board")
    public ResponseEntity<CustomResponse<Slice<ReaderBoardWithProfileInfo>>> getAllReaderBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") FeedSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(feedUseCase.getAllReaderBoard(authUserDetails.getUserId(), pageable));
    }

    @Operation(summary = "[관심 작품] 관심 작품 리스트 조회", description = "관심 작품 리스트를 조회하는 api 입니다. 무한스크롤 형식입니다.")
    @GetMapping("/reader/board/favorite/works")
    public ResponseEntity<CustomResponse<Slice<SlicedWorksInfo>>> getFavoriteWorksList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam(defaultValue = "LATEST") ProfileSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body( feedUseCase.getSlicedFavoriteWorksInfo(authUserDetails.getUserId(), pageable));
    }

    @Operation(summary = "[관심 작품] 게시글 리스트 조회", description = "관심 작품 id로 관련 게시글을 조회합니다. 무한 스크롤로 구성됩니다.")
    @GetMapping("/reader/board/works/{worksId}")
    public ResponseEntity<CustomResponse<Slice<ReaderBoardWithProfileInfo>>> getReaderBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long worksId,
            @RequestParam(defaultValue = "LATEST") FeedSortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(feedUseCase.getReaderBoard(authUserDetails.getUserId(), worksId, pageable));
    }

    @Operation(summary = "[관심 작품] 게시글 상세 조회", description = "게시글 id로 상세 페이지를 조회합니다.")
    @GetMapping("/reader/board/{boardId}")
    public ResponseEntity<CustomResponse<BoardWrapperDto<ReaderBoardReplyInfoWithProfile>>> getReaderBoardDetail(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @RequestParam(defaultValue = "LATEST") ReplySortType sort,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());
        return ResponseEntity.ok()
                .body(feedUseCase.getReaderBoardDetail(authUserDetails.getUserId(), boardId, pageable));
    }

    @Operation(summary = "[관심 작품] 게시글 좋아요", description = "게시글 id로 좋아요를 토글링하는 api 입니다. 좋아요 여부와 최신 좋아요 수가 반환됩니다.")
    @PostMapping("/reader/board/{boardId}/like")
    public ResponseEntity<CustomResponse<LikeToggleResponse>> toggleReaderBoardLike(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.toggleReaderBoardLike(authUserDetails.getUserId(), boardId));
    }

    @Operation(summary = "[관심 작품] 댓글 좋아요", description = "댓글 id로 좋아요를 토글링하는 api 입니다. 좋아요 여부와 최신 좋아요 수가 반환됩니다.")
    @PostMapping("/reader/board/{boardId}/reply/{replyId}/like")
    public ResponseEntity<CustomResponse<LikeToggleResponse>> toggleReaderBoardReplyLike(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @PathVariable @NotNull Long replyId
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.toggleReaderBoardReplyLike(authUserDetails.getUserId(), boardId, replyId));
    }

    @Operation(summary = "[관심 작품] 댓글 작성", description = "댓글을 작성하는 api 입니다.")
    @PostMapping("/reader/board/{boardId}/reply")
    public ResponseEntity<CustomResponse<ReaderBoardReplyResponse>> writeReaderBoardReply(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @Valid @RequestBody ReaderBoardReplyRequest req
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.writeReaderBoardReply(authUserDetails.getUserId(), boardId, req));
    }

    @Operation(summary = "[케밥 메뉴] 독자 게시물 삭제", description = "독자 게시물을 삭제하는 api 입니다.")
    @DeleteMapping("/reader/board/{boardId}")
    public ResponseEntity<CustomResponse<Void>> deleteOwnBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId
    ) {
        return ResponseEntity.ok()
                .body(feedKebabUseCase.deleteOwnBoard(authUserDetails.getUserId(), boardId));
    }

    @Operation(summary = "[케밥 메뉴] 독자 댓글 삭제", description = "독자 댓글을 삭제하는 api 입니다.")
    @DeleteMapping("/reader/board/{boardId}/reply/{replyId}")
    public ResponseEntity<CustomResponse<Void>> deleteOwnReply(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @PathVariable @NotNull Long replyId
    ) {
        return ResponseEntity.ok()
                .body(feedKebabUseCase.deleteOwnReply(authUserDetails.getUserId(), boardId, replyId));
    }

    @Operation(summary = "[케밥 메뉴] 독자 게시물 신고", description = "독자 게시물을 신고하는 api 입니다.")
    @PostMapping("/reader/board/{boardId}/report")
    public ResponseEntity<CustomResponse<Void>> report(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @Valid @RequestBody FeedReportRequest req
    ) {
        return ResponseEntity.ok()
                .body(feedKebabUseCase.reportFeed(authUserDetails.getUserId(), boardId, req));
    }

    @Operation(summary = "[케밥 메뉴] 독자 댓글 신고", description = "독자 댓글을 신고하는 api 입니다.")
    @PostMapping("/reader/board/{boardId}/reply/{replyId}/report")
    public ResponseEntity<CustomResponse<Void>> reportReply(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @PathVariable @NotNull Long replyId,
            @Valid @RequestBody FeedReportRequest req
    ) {
        return ResponseEntity.ok()
                .body(feedKebabUseCase.reportFeedReply(authUserDetails.getUserId(), boardId, replyId, req));
    }

}
