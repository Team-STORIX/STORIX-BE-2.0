package com.storix.domain.domains.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
@Getter
public enum Role {
    READER("READER"),
    TESTER("TESTER"),
    ADMIN("ADMIN");

    private final String stringValue;

    /** 토큰 클레임처럼 신뢰할 수 없는 값에서 찾는다. 없으면 부르는 쪽이 맥락에 맞는 예외를 던진다. */
    public static Optional<Role> find(String stringValue) {
        return Arrays.stream(values())
                .filter(r -> r.stringValue.equals(stringValue))
                .findFirst();
    }
}
