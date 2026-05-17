package com.storix.domain.domains.feed.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardTheme {

    BIRTHDAY("생일 테마");

    private final String description;
}
