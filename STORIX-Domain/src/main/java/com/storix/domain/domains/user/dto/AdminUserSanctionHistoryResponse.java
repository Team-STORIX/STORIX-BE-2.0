package com.storix.domain.domains.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.user.domain.UserSanctionHistory;
import com.storix.domain.domains.user.domain.UserSanctionSource;
import com.storix.domain.domains.user.domain.UserSanctionType;

import java.time.LocalDate;

public record AdminUserSanctionHistoryResponse(
        UserSanctionType type,
        UserSanctionSource source,
        Long reportCaseId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startedAt,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endedAt,
        String memo
) {

    public static AdminUserSanctionHistoryResponse from(UserSanctionHistory history) {
        return new AdminUserSanctionHistoryResponse(
                history.getType(),
                history.getSource(),
                history.getReportCaseId(),
                history.getStartedAt().toLocalDate(),
                history.getEndedAt() == null ? null : history.getEndedAt().toLocalDate(),
                history.getMemo()
        );
    }
}
