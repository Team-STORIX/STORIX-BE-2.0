package com.storix.domain.domains.user.domain;

import com.storix.domain.domains.user.exception.auth.InvalidRoleException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Role {
    READER("READER"),
    ARTIST("ARTIST");

    private final String stringValue;

    public static Role fromValue(String stringValue) {
        return Arrays.stream(values())
                .filter(r -> r.stringValue.equals(stringValue))
                .findFirst()
                .orElseThrow(() -> InvalidRoleException.EXCEPTION);
    }
}
