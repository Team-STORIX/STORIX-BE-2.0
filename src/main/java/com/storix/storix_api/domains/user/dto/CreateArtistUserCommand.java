package com.storix.storix_api.domains.user.dto;

import com.storix.storix_api.domains.user.domain.User;

public record CreateArtistUserCommand(
        String nickName,
        String loginId,
        String password
) {
    public User toEntity(){
        return new User(
                nickName,
                loginId,
                password
        );
    }
}
