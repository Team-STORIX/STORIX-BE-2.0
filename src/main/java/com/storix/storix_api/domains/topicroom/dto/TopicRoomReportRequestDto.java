package com.storix.storix_api.domains.topicroom.dto;

import com.storix.storix_api.domains.topicroom.domain.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopicRoomReportRequestDto {

    @NotNull
    private Long reportedUserId;

    @NotNull
    private ReportReason reason;

    @Size(max = 100)
    private String otherReason;
}
