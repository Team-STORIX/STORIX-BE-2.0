package com.storix.domain.domains.topicroom.domain.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportReason {

    ABUSE("욕설/비하"),
    SPAM("스팸"),
    OTHER("기타");

    private final String description;
}
