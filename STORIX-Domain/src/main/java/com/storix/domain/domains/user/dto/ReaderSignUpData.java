package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.works.domain.Genre;

import java.util.Set;

// v2: 약관별 동의(서비스 이용약관/개인정보 수집·이용) + 만 14세 이상 동의를 개별로 수신
// v1(deprecated): 약관/연령 동의 필드 사용 X
public record ReaderSignUpData(
        Boolean serviceTermsAgree,
        Boolean privacyPolicyAgree,
        Boolean ageOver14,
        String nickName,
        String profileDescription,
        Set<Genre> favoriteGenreList,
        Set<Long> favoriteWorksIdList
) {
}
