package com.storix.api.domain.feed.controller;

import com.storix.api.domain.feed.controller.dto.FeedReportRequest;
import com.storix.api.domain.feed.controller.dto.ReaderBoardReplyRequest;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyResponse;
import com.storix.domain.domains.feed.domain.FeedSortType;
import com.storix.domain.domains.feed.domain.ReplySortType;
import com.storix.domain.domains.feed.dto.BoardWrapperDto;
import com.storix.domain.domains.feed.dto.BookmarkResponse;
import com.storix.domain.domains.feed.dto.LikeToggleResponse;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.api.domain.feed.usecase.FeedKebabUseCase;
import com.storix.api.domain.feed.usecase.FeedReactionUseCase;
import com.storix.api.domain.feed.usecase.FeedSearchUseCase;
import com.storix.api.domain.feed.usecase.FeedUseCase;
import com.storix.domain.domains.profile.dto.ProfileSortType;
import com.storix.domain.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.domain.domains.search.dto.RecentResponseDto;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.works.dto.SlicedWorksInfo;
import com.storix.common.payload.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    private final FeedSearchUseCase feedSearchUseCase;

    @Operation(summary = "전체 게시물 리스트 조회", description = "전체 게시물 리스트를 조회하는 api 입니다. 무한 스크롤로 구성됩니다.")
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

    @Operation(summary = "피드 게시글 검색", description = "본문과 연결된 작품명으로 게시글을 관련도순으로 검색합니다. 관련도가 같으면 최신순이고, 차단한 유저의 글은 빠집니다. 첫 페이지 검색어는 피드 최근 검색어에 저장됩니다.")
    @GetMapping("/reader/board/search")
    public ResponseEntity<CustomResponse<Slice<ReaderBoardWithProfileInfo>>> searchReaderBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        return ResponseEntity.ok()
                .body(feedSearchUseCase.searchReaderBoards(authUserDetails.getUserId(), keyword, pageable));
    }

    @Operation(summary = "피드 최근 검색어 조회", description = "피드 검색에서 최근에 검색한 키워드를 최신순으로 최대 10개 조회합니다.")
    @GetMapping("/search/recent")
    public ResponseEntity<CustomResponse<RecentResponseDto>> getFeedRecentKeywords(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(feedSearchUseCase.getRecentKeywords(authUserDetails.getUserId()));
    }

    @Operation(summary = "피드 최근 검색어 삭제", description = "피드 최근 검색어 하나를 삭제합니다.")
    @DeleteMapping("/search/recent")
    public ResponseEntity<CustomResponse<Void>> deleteFeedRecentKeyword(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestParam @NotBlank String keyword
    ) {
        return ResponseEntity.ok()
                .body(feedSearchUseCase.deleteRecentKeyword(authUserDetails.getUserId(), keyword));
    }

    @Operation(summary = "피드 최근 검색어 전체 삭제", description = "피드 최근 검색어를 모두 삭제합니다.")
    @DeleteMapping("/search/recent/all")
    public ResponseEntity<CustomResponse<Void>> deleteAllFeedRecentKeywords(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        return ResponseEntity.ok()
                .body(feedSearchUseCase.deleteAllRecentKeywords(authUserDetails.getUserId()));
    }

    @Operation(summary = "관심 작품 리스트 조회", description = "관심 작품 리스트를 조회하는 api 입니다. 무한스크롤 형식입니다.")
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

    @Operation(summary = "관심 작품 관련 게시물 리스트 조회", description = "관심 작품 id로 관련 게시글을 조회합니다. 무한 스크롤로 구성됩니다.")
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

    @Operation(summary = "게시글 상세 조회", description = "게시글 id로 상세 페이지를 조회합니다.")
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

    @Operation(summary = "게시글 좋아요", description = "게시글 id로 좋아요를 토글링하는 api 입니다. 좋아요 여부와 최신 좋아요 수가 반환됩니다.")
    @PostMapping("/reader/board/{boardId}/like")
    public ResponseEntity<CustomResponse<LikeToggleResponse>> toggleReaderBoardLike(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.toggleReaderBoardLike(authUserDetails.getUserId(), boardId));
    }

    @Operation(summary = "게시글 북마크 등록", description = "게시글 id로 북마크를 등록 api 입니다. 이미 북마크한 게시글이면 그대로 두고 북마크 여부와 최신 북마크 수가 반환됩니다.")
    @PutMapping("/reader/board/{boardId}/bookmark")
    public ResponseEntity<CustomResponse<BookmarkResponse>> bookmarkReaderBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.bookmarkReaderBoard(authUserDetails.getUserId(), boardId));
    }

    @Operation(summary = "게시글 북마크 해제", description = "게시글 id로 북마크를 해제하는 api 입니다. 북마크하지 않은 게시글이면 그대로 두고 북마크 여부와 최신 북마크 수가 반환됩니다.")
    @DeleteMapping("/reader/board/{boardId}/bookmark")
    public ResponseEntity<CustomResponse<BookmarkResponse>> unbookmarkReaderBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.unbookmarkReaderBoard(authUserDetails.getUserId(), boardId));
    }

    @Operation(summary = "댓글 좋아요", description = "댓글 id로 좋아요를 토글링하는 api 입니다. 좋아요 여부와 최신 좋아요 수가 반환됩니다.")
    @PostMapping("/reader/board/{boardId}/reply/{replyId}/like")
    public ResponseEntity<CustomResponse<LikeToggleResponse>> toggleReaderBoardReplyLike(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @PathVariable @NotNull Long replyId
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.toggleReaderBoardReplyLike(authUserDetails.getUserId(), boardId, replyId));
    }

    @Operation(summary = "댓글 작성", description = "댓글을 작성하는 api 입니다.")
    @PostMapping("/reader/board/{boardId}/reply")
    public ResponseEntity<CustomResponse<ReaderBoardReplyResponse>> writeReaderBoardReply(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @Valid @RequestBody ReaderBoardReplyRequest req
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.writeReaderBoardReply(authUserDetails.getUserId(), boardId, req));
    }

    @Operation(summary = "답댓글 작성", description = "댓글에 대한 답댓글을 작성하는 api 입니다. depth 1까지만 허용됩니다 (답댓글에 답댓글 불가).")
    @PostMapping("/reader/board/{boardId}/reply/{replyId}/reply")
    public ResponseEntity<CustomResponse<ReaderBoardReplyResponse>> writeReaderBoardChildReply(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @PathVariable @NotNull Long replyId,
            @Valid @RequestBody ReaderBoardReplyRequest req
    ) {
        return ResponseEntity.ok()
                .body(feedReactionUseCase.writeReaderBoardChildReply(authUserDetails.getUserId(), boardId, replyId, req));
    }

    @Operation(summary = "[케밥 메뉴] 게시물 삭제", description = "게시물을 삭제하는 api 입니다.")
    @DeleteMapping("/reader/board/{boardId}")
    public ResponseEntity<CustomResponse<Void>> deleteOwnBoard(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId
    ) {
        return ResponseEntity.ok()
                .body(feedKebabUseCase.deleteOwnBoard(authUserDetails.getUserId(), boardId));
    }

    @Operation(summary = "[케밥 메뉴] 댓글 삭제", description = "댓글을 삭제하는 api 입니다.")
    @DeleteMapping("/reader/board/{boardId}/reply/{replyId}")
    public ResponseEntity<CustomResponse<Void>> deleteOwnReply(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @PathVariable @NotNull Long replyId
    ) {
        return ResponseEntity.ok()
                .body(feedKebabUseCase.deleteOwnReply(authUserDetails.getUserId(), boardId, replyId));
    }

    @Operation(summary = "[케밥 메뉴] 게시물 신고", description = "게시물을 신고하는 api 입니다.")
    @PostMapping("/reader/board/{boardId}/report")
    public ResponseEntity<CustomResponse<Void>> report(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @PathVariable @NotNull Long boardId,
            @Valid @RequestBody FeedReportRequest req
    ) {
        return ResponseEntity.ok()
                .body(feedKebabUseCase.reportFeed(authUserDetails.getUserId(), boardId, req));
    }

    @Operation(summary = "[케밥 메뉴] 댓글 신고", description = "댓글을 신고하는 api 입니다.")
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
