package com.storix.domain.domains.works.dto;

public record WorksRenameResult(
        Long worksId,
        String beforeName,
        String afterName
) {
}
