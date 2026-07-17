package com.storix.domain.domains.topicroom.domain.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportReason {

    SPAM("스팸"),
    ABUSE("욕설/비하"),
    OTHER("기타"),
    DEFAULT("기본");

    private final String description;
}
