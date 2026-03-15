package com.storix.domain.domains.works.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Genre {

    ROMANCE("로맨스"),
    FANTASY("판타지"),
    DAILY("일상"),
    ROFAN("로판"),
    HISTORICAL("무협/사극"),
    DRAMA("드라마"),
    GAG("개그"),
    THRILLER("스릴러"),
    ACTION("액션"),
    SPORTS("스포츠"),
    SENTIMENTAL("감성"),
    BL("BL"),
    MODERN_FANTASY("현판");

    private final String dbValue;
}
