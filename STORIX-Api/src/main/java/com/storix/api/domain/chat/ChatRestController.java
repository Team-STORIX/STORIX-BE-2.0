package com.storix.api.domain.chat;

import com.storix.domain.domains.chat.application.usecase.ChatUseCase;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "토픽룸 - 채팅", description = "토픽룸 채팅 REST API")
public class ChatRestController {

    private final ChatUseCase chatUseCase;

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "채팅방 메시지 조회", description = "과거 메시지를 불러옵니다. 페이지네이션 디폴트 값을 참고해 주세요.")
    public CustomResponse<Slice<ChatMessageResponseDto>> getChatHistory(
            @PathVariable Long roomId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<ChatMessageResponseDto> history = chatUseCase.getChatHistory(roomId, pageable);
        return CustomResponse.onSuccess(SuccessCode.SUCCESS, history);
    }
}