package com.storix.api.domain.search;

import com.storix.domain.domains.search.application.SearchUseCase;
import com.storix.domain.domains.search.dto.PlusSearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.search.dto.WorksSearchResponseDto;
import com.storix.domain.domains.topicroom.application.usecase.TopicRoomUseCase;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomSortType;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksSortType;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/search")
@RequiredArgsConstructor
@Tag(name="검색", description = "검색 관련 API")
public class SearchV2Controller {

    private final SearchUseCase searchUseCase;
    private final TopicRoomUseCase topicRoomUseCase;

    @GetMapping("/works")
    @Operation(summary = "작품 다중 필터 검색", description = "작품 유형, 장르 필터 + 정렬 기준으로 검색합니다.    \nSwagger 테스트 시 ctrl/cmd + 클릭으로 다중 선택이 가능합니다.")
    public CustomResponse<SearchResponseWrapperDto<WorksSearchResponseDto>> searchWorksWithFilters(
            @AuthenticationPrincipal AuthUserDetails authUser,

            @Parameter(description = "작품명")
            @RequestParam String keyword,

            @Parameter(description = "작품 유형 (다중 선택 가능)")
            @RequestParam(required = false) List<WorksType> worksTypes,

            @Parameter(description = "장르 (다중 선택 가능)")
            @RequestParam(required = false) List<Genre> genres,

            @Parameter(description = "정렬 기준")
            @RequestParam(defaultValue = "NAME") WorksSortType sort,

            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                searchUseCase.searchWorksWithFilters(authUser.getUserId(), keyword, worksTypes, genres, pageable)
        );
    }

    // 토픽룸 다중 필터 검색
    @GetMapping("/topic-rooms")
    @Operation(summary = "토픽룸 다중 필터 검색", description = "토픽룸 검색 리스트를 반환합니다.")
    public CustomResponse<PlusSearchResponseWrapperDto<TopicRoomResponseDto>> search(
            @AuthenticationPrincipal AuthUserDetails authUser,

            @Parameter(description = "작품명")
            @RequestParam String keyword,

            @Parameter(description = "작품 유형 (다중 선택 가능)")
            @RequestParam(required = false) List<WorksType> worksTypes,

            @Parameter(description = "장르 (다중 선택 가능)")
            @RequestParam(required = false) List<Genre> genres,

            @Parameter(description = "정렬 기준")
            @RequestParam(defaultValue = "DEFAULT") TopicRoomSortType sort,

            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10, sort.getSortValue());

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                topicRoomUseCase.searchRoomsWithFilters(authUser.getUserId(), keyword, worksTypes, genres, pageable)
        );
    }
}
