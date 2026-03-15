package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.WorksType;

public record TopicRoomWorksInfo(
        Long id,
        String worksName,
        String imageUrl,
        WorksType worksType
){
}
