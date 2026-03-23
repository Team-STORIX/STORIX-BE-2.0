package com.storix.api.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.api.domain.review.usecase.WorksDetailKebabUseCase;
import com.storix.api.domain.review.usecase.WorksDetailReactionUseCase;
import com.storix.api.domain.review.usecase.WorksDetailReviewUseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.plus.domain.Rating;
import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXCodeException;
import com.storix.common.payload.ErrorResponse;
import com.storix.domain.domains.plus.exception.SpoilerScriptRequiredException;
import com.storix.domain.domains.review.dto.ModifyReviewRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("[E2E] 스포일러 문구 커스텀 기능")
class SpoilerCustomE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WorksDetailReviewUseCase worksDetailReviewUseCase;

    @Mock
    private WorksDetailKebabUseCase worksDetailKebabUseCase;

    @Mock
    private WorksDetailReactionUseCase worksDetailReactionUseCase;

    @InjectMocks
    private ReviewController reviewController;

    private static final Long USER_ID = 1L;
    private static final Long REVIEW_ID = 10L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthUserDetailsArgumentResolver()
                )
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    // ===== 스포일러 커스텀 문구와 함께 리뷰 수정 =====

    @Nested
    @DisplayName("리뷰 수정 - 스포일러 커스텀 문구")
    class ModifyReviewWithSpoiler {

        @Test
        @DisplayName("성공: 스포일러 ON + 커스텀 문구 입력하여 리뷰 수정")
        void modifyReview_withSpoilerScript_success() throws Exception {
            // given
            ModifyReviewRequest request = new ModifyReviewRequest(
                    Rating.FOUR, true, "100화 스포, 대형 스포이니 주의!", "정말 재미있는 작품입니다. 결말이 충격적이에요."
            );

            CustomResponse<Long> customResponse =
                    CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_UPDATE_SUCCESS, REVIEW_ID);

            given(worksDetailKebabUseCase.modifyMyReview(eq(USER_ID), eq(REVIEW_ID), any()))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result").value(REVIEW_ID));
        }

        @Test
        @DisplayName("성공: 스포일러 OFF + 문구 없이 리뷰 수정 (기본 동작)")
        void modifyReview_withoutSpoiler_success() throws Exception {
            // given
            ModifyReviewRequest request = new ModifyReviewRequest(
                    Rating.FIVE, false, null, "스포 없는 일반 리뷰입니다."
            );

            CustomResponse<Long> customResponse =
                    CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_UPDATE_SUCCESS, REVIEW_ID);

            given(worksDetailKebabUseCase.modifyMyReview(eq(USER_ID), eq(REVIEW_ID), any()))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result").value(REVIEW_ID));
        }

        @Test
        @DisplayName("실패: 스포일러 ON인데 커스텀 문구가 빈 문자열이면 에러")
        void modifyReview_spoilerOnButEmptyScript_fail() throws Exception {
            // given
            ModifyReviewRequest request = new ModifyReviewRequest(
                    Rating.THREE, true, "", "스포일러 내용이 있는 리뷰"
            );

            given(worksDetailKebabUseCase.modifyMyReview(eq(USER_ID), eq(REVIEW_ID), any()))
                    .willThrow(SpoilerScriptRequiredException.EXCEPTION);

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 스포일러 ON인데 커스텀 문구가 null이면 에러")
        void modifyReview_spoilerOnButNullScript_fail() throws Exception {
            // given
            ModifyReviewRequest request = new ModifyReviewRequest(
                    Rating.THREE, true, null, "스포일러 내용이 있는 리뷰"
            );

            given(worksDetailKebabUseCase.modifyMyReview(eq(USER_ID), eq(REVIEW_ID), any()))
                    .willThrow(SpoilerScriptRequiredException.EXCEPTION);

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("성공: 스포일러 OFF로 변경하면 spoilerScript가 무시된다")
        void modifyReview_spoilerOff_scriptIgnored_success() throws Exception {
            // given
            ModifyReviewRequest request = new ModifyReviewRequest(
                    Rating.FOUR_POINT_FIVE, false, "이건 무시될 문구", "스포일러 해제된 리뷰"
            );

            CustomResponse<Long> customResponse =
                    CustomResponse.onSuccess(SuccessCode.WORKS_DETAIL_REVIEW_UPDATE_SUCCESS, REVIEW_ID);

            given(worksDetailKebabUseCase.modifyMyReview(eq(USER_ID), eq(REVIEW_ID), any()))
                    .willReturn(customResponse);

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("실패: 리뷰 내용이 비어있으면 400 에러")
        void modifyReview_emptyContent_fail() throws Exception {
            // given
            ModifyReviewRequest request = new ModifyReviewRequest(
                    Rating.FOUR, true, "스포일러 문구", ""
            );

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 별점이 null이면 400 에러")
        void modifyReview_nullRating_fail() throws Exception {
            // given
            String requestJson = """
                    {
                        "rating": null,
                        "isSpoiler": true,
                        "spoilerScript": "스포 주의",
                        "content": "리뷰 내용"
                    }
                    """;

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 리뷰 내용이 500자 초과하면 400 에러")
        void modifyReview_tooLongContent_fail() throws Exception {
            // given
            String longContent = "가".repeat(501);
            ModifyReviewRequest request = new ModifyReviewRequest(
                    Rating.FOUR, false, null, longContent
            );

            // when & then
            mockMvc.perform(patch("/api/v1/works/review/{reviewId}", REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * STORIXCodeException을 처리하는 테스트용 예외 핸들러
     */
    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(STORIXCodeException.class)
        public ResponseEntity<ErrorResponse> handleSTORIXCodeException(STORIXCodeException ex) {
            ErrorCode errorCode = ex.getErrorCode();
            return ResponseEntity.status(errorCode.getHttpStatus()).body(new ErrorResponse(errorCode));
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
