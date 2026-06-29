package com.storix.domain.domains.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.user.domain.UserSanctionHistory;
import com.storix.domain.domains.user.domain.UserSanctionSource;
import com.storix.domain.domains.user.domain.UserSanctionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record AdminUserSanctionDetailResponse(
        Long sanctionId,
        UserSanctionType type,
        UserSanctionSource source,
        Long reportCaseId,
        Long adminId,
        String adminNickName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startedAt,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endedAt,
        String memo,
        LocalDateTime createdAt
) {

    public static AdminUserSanctionDetailResponse from(UserSanctionHistory history, Map<Long, String> adminNickNames) {
        Long adminId = history.getAdminId();
        return new AdminUserSanctionDetailResponse(
                history.getId(),
                history.getType(),
                history.getSource(),
                history.getReportCaseId(),
                adminId,
                adminId == null ? null : adminNickNames.get(adminId),
                history.getStartedAt().toLocalDate(),
                history.getEndedAt() == null ? null : history.getEndedAt().toLocalDate(),
                history.getMemo(),
                history.getCreatedAt()
        );
    }
}
