package com.storix.storix_api.domains.topicroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopicRoomCreateRequestDto {

    @NotNull(message = "작품 선택은 필수입니다.")
    private Long worksId;

    @NotNull(message = "토픽룸 제목 입력은 필수입니다.")
    private String topicRoomName;
}
