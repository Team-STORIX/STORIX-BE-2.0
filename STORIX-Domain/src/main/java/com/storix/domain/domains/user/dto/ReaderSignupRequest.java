package com.storix.domain.domains.user.dto;

import com.storix.domain.domains.works.domain.Genre;
import jakarta.validation.constraints.*;

import java.util.Set;

public record DeveloperSignupRequest(

    @NotBlank(message = "개발자 식별자는 필수입니다.")
    String developerIdentifier,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자까지 가능합니다.")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ ]+$",
            message = "닉네임은 한글, 영문, 숫자, 공백만 가능합니다."
    )
    String nickName,

<<<<<<<< HEAD:STORIX-Domain/src/main/java/com/storix/domain/domains/user/dto/DeveloperSignupRequest.java
========
    @Size(max = 30, message = "한 줄 소개는 30자까지 가능합니다.")
    String profileDescription,

>>>>>>>> 1d6df8f7740d028d22210d6b0d6322808788f943:STORIX-Domain/src/main/java/com/storix/domain/domains/user/dto/ReaderSignupRequest.java
    @NotNull(message = "관심 장르는 필수입니다.")
    @Size(min = 1, max = 3, message = "관심 장르는 1개 이상 3개 이하로 선택해야 합니다.")
    Set<Genre> favoriteGenreList,

<<<<<<<< HEAD:STORIX-Domain/src/main/java/com/storix/domain/domains/user/dto/DeveloperSignupRequest.java
    @Size(min = 2, max = 18, message = "관심 작품은 2개 이상 18개 이하로 선택해야 합니다.")
========
    @Size(max = 18, message = "관심 작품은 18개 이하로 선택해야 합니다.")
>>>>>>>> 1d6df8f7740d028d22210d6b0d6322808788f943:STORIX-Domain/src/main/java/com/storix/domain/domains/user/dto/ReaderSignupRequest.java
    Set<Long> favoriteWorksIdList

) {
}
