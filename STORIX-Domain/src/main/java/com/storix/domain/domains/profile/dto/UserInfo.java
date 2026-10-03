package com.storix.domain.domains.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.user.domain.User;
import lombok.Builder;

@Builder
public record UserInfo(
    Long userId,
    String role,
    String profileImageUrl,
    String nickName,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer level,

    String profileDescription,

    String oauthProvider
) {
    public static UserInfo of(User user, String baseUrl) {
        return UserInfo.builder()
                .userId(user.getId())
                .role(user.getRole().toString())
                .nickName(user.getDisplayNickName())
                .level(1)// level 미사용
                .profileDescription(user.getProfileDescription())
                .profileImageUrl(user.getProfileObjectKey() == null
                        ? null : baseUrl + "/" + user.getProfileObjectKey())
                .oauthProvider(user.getOauthInfo() == null
                        ? null : user.getOauthInfo().getProvider().getDbValue())
                .build();
    }
}
