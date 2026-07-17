package com.storix.domain.domains.image.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventImageSurface {

    POPUP("popup"),
    BANNER("banner");

    private final String value;
}
