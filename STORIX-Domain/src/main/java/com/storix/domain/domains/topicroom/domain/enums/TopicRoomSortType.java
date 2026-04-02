package com.storix.domain.domains.topicroom.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum TopicRoomSortType {

    DEFAULT("기본순", Sort.by(Sort.Direction.ASC, "id")),
    LATEST("최신순", Sort.by(
            Sort.Order.desc("lastChatTime"),
            Sort.Order.asc("id"))
    ),
    ACTIVE("참여순", Sort.by(
            Sort.Order.desc("activeUserNumber"),
            Sort.Order.asc("id"))
    );

    private final String description;
    private final Sort sortValue;
}
