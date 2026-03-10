package com.storix.domain.domains.works.dto;

public record StandardWorksInfo(
        Long worksId,
        String thumbnailUrl,
        String worksName,
        String artistName,
        String worksType,
        String genre
) {
    public static StandardWorksInfo from(WorksInfo worksInfo) {
        if (worksInfo == null) {
            return null;
        }

        return new StandardWorksInfo(
                worksInfo.worksId(),
                worksInfo.thumbnailUrl(),
                worksInfo.worksName(),
                worksInfo.artistName(),
                worksInfo.worksType().getDbValue(),
                worksInfo.genre().getDbValue()
        );
    }
}
