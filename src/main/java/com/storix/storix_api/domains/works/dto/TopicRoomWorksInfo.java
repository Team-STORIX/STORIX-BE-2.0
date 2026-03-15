package com.storix.storix_api.domains.works.dto;

import com.storix.storix_api.domains.works.domain.WorksType;

public record TopicRoomWorksInfo(
        Long id,
        String worksName,
        String imageUrl,
        WorksType worksType
){
}
