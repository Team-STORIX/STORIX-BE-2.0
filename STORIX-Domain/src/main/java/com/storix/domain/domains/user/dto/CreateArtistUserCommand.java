package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.user.domain.User;

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
