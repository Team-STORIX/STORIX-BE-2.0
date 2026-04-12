package com.storix.api.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.api.domain.feed.controller.dto.FeedReportRequest;
import com.storix.api.domain.feed.controller.dto.ReaderBoardReplyRequest;
import com.storix.api.domain.feed.usecase.FeedKebabUseCase;
import com.storix.api.domain.feed.usecase.FeedReactionUseCase;
import com.storix.api.domain.feed.usecase.FeedUseCase;
import com.storix.common.code.ErrorCode;
import com.storix.common.code.SuccessCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.common.payload.CustomResponse;
import com.storix.common.payload.ErrorResponse;
import com.storix.domain.domains.feed.dto.LikeToggleResponse;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyResponse;
import com.storix.domain.domains.feed.dto.StandardReplyInfo;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[E2E] 피드 답댓글 기능")
class FeedReplyE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FeedUseCase feedUseCase;

    @Mock
    private FeedReactionUseCase feedReactionUseCase;

    @Mock
    private FeedKebabUseCase feedKebabUseCase;

    @InjectMocks
    private FeedController feedController;

    private static final Long USER_ID = 1L;
    private static final Long BOARD_ID = 100L;
    private static final Long REPLY_ID = 200L;
    private static final Long CHILD_REPLY_ID = 300L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(feedController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthUserDetailsArgumentResolver()
                )
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    // ===== 답댓글 작성 =====

    @Nested
    @DisplayName("답댓글 작성 API")
    class WriteChildReply {

        @Test
        @DisplayName("성공: 댓글에 답댓글을 작성한다")
        void writeChildReply_success() throws Exception {
            // given
            StandardReplyInfo replyInfo = new StandardReplyInfo(
                    CHILD_REPLY_ID, "답댓글 테스트입니다", 0, 1, 0, REPLY_ID, false
            );
            ReaderBoardReplyResponse response = new ReaderBoardReplyResponse(null, replyInfo);
            CustomResponse<ReaderBoardReplyResponse> customResponse =
                    CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_UPLOAD_SUCCESS, response);

            given(feedReactionUseCase.writeReaderBoardChildReply(eq(USER_ID), eq(BOARD_ID), eq(REPLY_ID), any()))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(post("/api/v1/feed/reader/board/{boardId}/reply/{replyId}/reply", BOARD_ID, REPLY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ReaderBoardReplyRequest("답댓글 테스트입니다"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.content.replyId").value(CHILD_REPLY_ID))
                    .andExpect(jsonPath("$.result.content.depth").value(1))
                    .andExpect(jsonPath("$.result.content.parentReplyId").value(REPLY_ID));
        }

        @Test
        @DisplayName("실패: 답댓글에 답댓글을 작성하면 에러가 발생한다 (depth 1 제한)")
        void writeNestedChildReply_depthExceeded_fail() throws Exception {
            // given
            given(feedReactionUseCase.writeReaderBoardChildReply(eq(USER_ID), eq(BOARD_ID), eq(CHILD_REPLY_ID), any()))
                    .willThrow(new com.storix.common.exception.STORIXCodeException(
                            com.storix.common.code.ErrorCode.REPLY_DEPTH_EXCEEDED));

            // when & then
            mockMvc.perform(post("/api/v1/feed/reader/board/{boardId}/reply/{replyId}/reply", BOARD_ID, CHILD_REPLY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ReaderBoardReplyRequest("대댓글의 대댓글"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 빈 댓글로 답댓글 작성 시 400 에러")
        void writeChildReply_emptyComment_fail() throws Exception {
            mockMvc.perform(post("/api/v1/feed/reader/board/{boardId}/reply/{replyId}/reply", BOARD_ID, REPLY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ReaderBoardReplyRequest(""))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 300자 초과 댓글로 답댓글 작성 시 400 에러")
        void writeChildReply_tooLongComment_fail() throws Exception {
            String longComment = "가".repeat(301);
            mockMvc.perform(post("/api/v1/feed/reader/board/{boardId}/reply/{replyId}/reply", BOARD_ID, REPLY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ReaderBoardReplyRequest(longComment))))
                    .andExpect(status().isBadRequest());
        }
    }

    // ===== 원댓글 작성 =====

    @Nested
    @DisplayName("원댓글 작성 API")
    class WriteReply {

        @Test
        @DisplayName("성공: 피드에 원댓글을 작성한다")
        void writeReply_success() throws Exception {
            // given
            StandardReplyInfo replyInfo = new StandardReplyInfo(
                    REPLY_ID, "원댓글 테스트", 0, 0, 0, null, false
            );
            ReaderBoardReplyResponse response = new ReaderBoardReplyResponse(null, replyInfo);
            CustomResponse<ReaderBoardReplyResponse> customResponse =
                    CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_UPLOAD_SUCCESS, response);

            given(feedReactionUseCase.writeReaderBoardReply(eq(USER_ID), eq(BOARD_ID), any()))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(post("/api/v1/feed/reader/board/{boardId}/reply", BOARD_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ReaderBoardReplyRequest("원댓글 테스트"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.content.depth").value(0))
                    .andExpect(jsonPath("$.result.content.parentReplyId").doesNotExist());
        }
    }

    // ===== 답댓글 좋아요 =====

    @Nested
    @DisplayName("답댓글 좋아요 API")
    class ReplyLike {

        @Test
        @DisplayName("성공: 답댓글에 좋아요를 토글한다")
        void toggleReplyLike_success() throws Exception {
            // given
            LikeToggleResponse likeResponse = new LikeToggleResponse(true, 1);
            CustomResponse<LikeToggleResponse> customResponse =
                    CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_LIKE_SUCCESS, likeResponse);

            given(feedReactionUseCase.toggleReaderBoardReplyLike(eq(USER_ID), eq(BOARD_ID), eq(CHILD_REPLY_ID)))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(post("/api/v1/feed/reader/board/{boardId}/reply/{replyId}/like", BOARD_ID, CHILD_REPLY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.isLiked").value(true))
                    .andExpect(jsonPath("$.result.likeCount").value(1));
        }
    }

    // ===== 댓글 삭제 (소프트 삭제) =====

    @Nested
    @DisplayName("댓글 삭제 API")
    class DeleteReply {

        @Test
        @DisplayName("성공: 본인 댓글을 삭제한다 (하위 답댓글 있을 때 소프트 삭제)")
        void deleteReply_softDelete_success() throws Exception {
            // given
            CustomResponse<Void> customResponse =
                    CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_DELETE_SUCCESS);

            given(feedKebabUseCase.deleteOwnReply(eq(USER_ID), eq(BOARD_ID), eq(REPLY_ID)))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(delete("/api/v1/feed/reader/board/{boardId}/reply/{replyId}", BOARD_ID, REPLY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }
    }

    // ===== 댓글 신고 =====

    @Nested
    @DisplayName("댓글 신고 API")
    class ReportReply {

        @Test
        @DisplayName("성공: 답댓글을 신고한다")
        void reportReply_success() throws Exception {
            // given
            CustomResponse<Void> customResponse =
                    CustomResponse.onSuccess(SuccessCode.FEED_READER_BOARD_REPLY_REPORT_SUCCESS);

            given(feedKebabUseCase.reportFeedReply(eq(USER_ID), eq(BOARD_ID), eq(CHILD_REPLY_ID), any()))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(post("/api/v1/feed/reader/board/{boardId}/reply/{replyId}/report", BOARD_ID, CHILD_REPLY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new FeedReportRequest(99L))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }
    }

    /**
     * STORIXCodeException을 처리하는 테스트용 예외 핸들러
     */
    @org.springframework.web.bind.annotation.RestControllerAdvice
    static class TestExceptionHandler {

        @org.springframework.web.bind.annotation.ExceptionHandler(STORIXCodeException.class)
        public org.springframework.http.ResponseEntity<ErrorResponse> handleSTORIXCodeException(STORIXCodeException ex) {
            ErrorCode errorCode = ex.getErrorCode();
            return org.springframework.http.ResponseEntity.status(errorCode.getHttpStatus()).body(new ErrorResponse(errorCode));
        }
    }

    /**
     * @AuthenticationPrincipal AuthUserDetails를 standalone MockMvc에서 주입하기 위한 리졸버
     */
    static class AuthUserDetailsArgumentResolver implements org.springframework.web.method.support.HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(AuthUserDetails.class);
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                      org.springframework.web.context.request.NativeWebRequest webRequest,
                                      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
            return new AuthUserDetails(USER_ID, Role.READER);
        }
    }
}
