package com.storix.domain.domains.search.dto;

import com.storix.domain.domains.works.domain.WorksNickname;

import java.time.LocalDateTime;

public record WorksNicknameResponse(
        Long nicknameId,
        String nickname,
        LocalDateTime createdAt
) {

    public static WorksNicknameResponse from(WorksNickname worksNickname) {
        return new WorksNicknameResponse(
                worksNickname.getId(),
                worksNickname.getNickname(),
                worksNickname.getCreatedAt()
        );
    }
}
