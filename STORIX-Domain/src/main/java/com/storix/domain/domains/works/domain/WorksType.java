package com.storix.domain.domains.works.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorksType {

    WEBTOON("웹툰"),
    WEBNOVEL("웹소설"),
    COMIC("만화"),
    BOOK("단행본");

    private final String dbValue;

    // 글 작품의 그림 작가는 표지 일러스트라 작가 표기에서 뺌
    public boolean creditsIllustrator() {
        return this == WEBTOON || this == COMIC;
    }
}
