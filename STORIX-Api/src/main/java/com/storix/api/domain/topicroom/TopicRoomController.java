package com.storix.api.domain.topicroom;

import com.storix.domain.domains.search.dto.SearchResponseWrapperDto;
import com.storix.domain.domains.topicroom.application.usecase.TopicRoomUseCase;
import com.storix.domain.domains.topicroom.dto.TopicRoomCreateRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomPreviewResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomReportRequestDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.dto.TopicRoomUserResponseDto;
import com.storix.api.domain.topicroom.dto.TopicRoomNotificationRequest;
import com.storix.api.domain.topicroom.dto.TopicRoomNotificationResponse;
import com.storix.api.domain.topicroom.dto.TopicRoomUnreadResponse;
import com.storix.domain.domains.topicroom.service.TopicRoomUnreadService;
import com.storix.domain.domains.topicroom.service.TopicRoomUserService;
import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topic-rooms")
@RequiredArgsConstructor
@Tag(name = "토픽룸", description = "토픽룸 REST API")
public class TopicRoomController {

    private final TopicRoomUseCase topicRoomUseCase;
    private final TopicRoomUserService topicRoomUserService;
    private final TopicRoomUnreadService topicRoomUnreadService;

    // 1. 참여 목록
    @GetMapping("/me")
    @Operation(summary = "참여 중인 토픽룸 조회", description = "내가 참여 중인 토픽룸 리스트를 반환합니다.")
    public CustomResponse<Slice<TopicRoomResponseDto>> getMyRooms(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @ParameterObject @PageableDefault(size = 3, sort = "topicRoom.lastChatTime", direction = Sort.Direction.DESC) Pageable pageable) {

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                topicRoomUseCase.getMyJoinedRooms(authUser.getUserId(), pageable));
    }

    // 2. 오늘의 토픽룸
    @GetMapping("/today")
    @Operation(summary = "오늘의 토픽룸 조회", description = "오늘의 토픽룸 리스트를 반환합니다. 활성 사용자가 많은 토픽룸 3개가 포함됩니다.")
    public CustomResponse<List<TopicRoomResponseDto>> getTodayTop3(
            @AuthenticationPrincipal AuthUserDetails authUser
    ) {

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                topicRoomUseCase.getTodayTrendingRooms(authUser.getUserId())
        );
    }

    // 3. 검색
    @GetMapping("/search")
    @Operation(summary = "토픽룸 검색", description = "토픽룸 검색 리스트를 반환합니다.")
    public CustomResponse<SearchResponseWrapperDto<TopicRoomResponseDto>> search(
            @RequestParam String keyword,
            @AuthenticationPrincipal AuthUserDetails authUser,
            @ParameterObject @PageableDefault( size = 10, sort = "topicRoomName", direction = Sort.Direction.ASC) Pageable pageable) {

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                topicRoomUseCase.searchRooms(keyword, authUser.getUserId(), pageable));
    }

    // 4. 생성
    @PostMapping
    @Operation(summary = "토픽룸 생성", description = "토픽룸을 생성합니다. 작품 선택 및 제목 설정은 필수입니다.")
    public CustomResponse<Long> create(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @Valid @RequestBody TopicRoomCreateRequestDto request) {

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                topicRoomUseCase.createRoom(authUser.getUserId(), request));
    }

    // 5. 입장
    @PostMapping("/{roomId}/join")
    @Operation(summary = "토픽룸 입장", description = "토픽룸에 참여합니다. 한 사용자는 최대 9개까지 참여할 수 있습니다.")
    public CustomResponse<String> join(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long roomId) {

        topicRoomUseCase.joinRoom(authUser.getUserId(), roomId);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS);
    }

    // 6. 퇴장
    @DeleteMapping("/{roomId}/leave")
    @Operation(summary = "토픽룸 퇴장", description = "참여 중이던 토픽룸에서 퇴장합니다.")
    public CustomResponse<String> leave(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long roomId) {

        topicRoomUseCase.leaveRoom(authUser.getUserId(), roomId);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS);
    }

    // 7. 신고
    @PostMapping("/{roomId}/report")
    @Operation(summary = "토픽룸 사용자 신고", description = "토픽룸 사용자를 신고합니다. 사유는 3개 중 선택 가능하며, 기타 사유의 경우 최대 100자 제한입니다.")
    public CustomResponse<String> report(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long roomId,
            @Valid @RequestBody TopicRoomReportRequestDto request) {

        topicRoomUseCase.reportUser(authUser.getUserId(), roomId, request);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS);
    }

    // 8. 지금 핫한 토픽룸
    @GetMapping("/popular")
    @Operation(summary = "지금 핫한 토픽룸 조회", description = "토픽룸 depth로 들어왔을 때 보여지는 목록입니다. 활동 점수 상위 최대 15개까지 조회됩니다. 비로그인 사용자일 경우 isJoined가 무조건 false로 반환됩니다.")
    public CustomResponse<List<TopicRoomPreviewResponseDto>> getPopularRooms(
            @AuthenticationPrincipal AuthUserDetails authUserDetails
    ) {
        List<TopicRoomPreviewResponseDto> rooms = topicRoomUseCase.getPopularRooms(authUserDetails.getUserId());
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, rooms);
    }

    // 9. 토픽룸 참여자 목록 조회
    @Operation(summary = "토픽룸 참여자 목록 조회", description = "채팅방 참여자들의 최신 프로필 정보를 조회합니다.")
    @GetMapping("/{roomId}/members")
    public CustomResponse<List<TopicRoomUserResponseDto>> getRoomMembers(@PathVariable Long roomId) {
        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                topicRoomUserService.getRoomMembers(roomId));
    }

    // 10. 읽음 처리
    @PostMapping("/{roomId}/read")
    @Operation(summary = "토픽룸 읽음 처리", description = "토픽룸 진입 시 호출합니다. 해당 방의 읽지 않은 메시지를 모두 읽음 처리해 뱃지를 제거합니다.")
    public CustomResponse<String> markRead(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long roomId) {

        topicRoomUnreadService.markRoomRead(authUser.getUserId(), roomId);

        return CustomResponse.onSuccess(SuccessCode.SUCCESS);
    }

    // 11. 미읽음 존재 여부
    @GetMapping("/unread")
    @Operation(summary = "토픽룸 미읽음 여부 조회", description = "참여 중인 토픽룸 전체에 읽지 않은 메시지가 있는지 반환합니다. 소통 탭 상단 Red Dot 표시용입니다.")
    public CustomResponse<TopicRoomUnreadResponse> hasUnread(
            @AuthenticationPrincipal AuthUserDetails authUser) {

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                new TopicRoomUnreadResponse(topicRoomUnreadService.hasAnyUnread(authUser.getUserId())));
    }

    // 12. 토픽룸별 알림 설정
    @PatchMapping("/{roomId}/notification")
    @Operation(summary = "토픽룸 채팅 알림 설정", description = "토픽룸별 채팅 푸시 알림을 켜거나 끕니다. 기본값은 ON이며, 방을 나가면 설정도 함께 사라집니다.")
    public CustomResponse<String> changeNotification(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long roomId,
            @Valid @RequestBody TopicRoomNotificationRequest request) {

        topicRoomUserService.changeNotification(authUser.getUserId(), roomId, request.enabled());

        return CustomResponse.onSuccess(SuccessCode.SUCCESS);
    }

    @GetMapping("/{roomId}/notification")
    @Operation(summary = "토픽룸 채팅 알림 설정 조회", description = "해당 토픽룸의 채팅 푸시 알림 설정 상태를 반환합니다.")
    public CustomResponse<TopicRoomNotificationResponse> getNotification(
            @AuthenticationPrincipal AuthUserDetails authUser,
            @PathVariable Long roomId) {

        return CustomResponse.onSuccess(
                SuccessCode.SUCCESS,
                new TopicRoomNotificationResponse(
                        topicRoomUserService.isNotificationEnabled(authUser.getUserId(), roomId)));
    }
}
