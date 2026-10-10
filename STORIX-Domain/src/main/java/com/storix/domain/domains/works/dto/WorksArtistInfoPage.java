package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksArtistInfoPage(
        List<WorksArtistInfo> content,
        Long nextAfterWorksId
) {
    public static WorksArtistInfoPage of(List<WorksArtistInfo> content, int size) {
        return new WorksArtistInfoPage(
                content,
                content.size() < size ? null : content.get(content.size() - 1).worksId()
        );
    }
}
