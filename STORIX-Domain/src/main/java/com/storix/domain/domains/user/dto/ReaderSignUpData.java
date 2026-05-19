package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.works.domain.Genre;

import java.util.Set;

// 회원가입 내부 명령 (v1/v2 API DTO -> 공통 변환 후 AuthService 에 전달)
public record ReaderSignUpData(
        Boolean termsAgree,
        String nickName,
        String profileDescription,
        Set<Genre> favoriteGenreList,
        Set<Long> favoriteWorksIdList
) {
}
