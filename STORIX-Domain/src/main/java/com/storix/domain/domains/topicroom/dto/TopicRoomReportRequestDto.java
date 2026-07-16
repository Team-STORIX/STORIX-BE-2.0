package com.storix.domain.domains.topicroom.dto;

import com.storix.domain.domains.topicroom.domain.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopicRoomReportRequestDto {

    @NotNull
    private Long reportedUserId;

    private Long chatMessageId;

    // TO DO: 유저 신고 및 채팅 신고 시 DEFAULT로 들어가며, 기획 측 요청이 들어올 경우 리팩토링 예정
    private ReportReason reason;

    @Size(max = 100)
    private String otherReason;
}
