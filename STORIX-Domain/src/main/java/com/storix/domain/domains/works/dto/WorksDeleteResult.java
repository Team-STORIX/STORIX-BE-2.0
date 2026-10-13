package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksDeleteResult(
        boolean dryRun,
        List<WorksDeleteItem> works
) {

    public List<Long> deletedWorksIds() {
        return works.stream()
                .filter(item -> item.status() == WorksDeleteItem.Status.DELETED)
                .map(WorksDeleteItem::worksId)
                .toList();
    }

    public boolean onboardingDeleted() {
        return works.stream().anyMatch(item -> item.status() == WorksDeleteItem.Status.DELETED && item.onboarding());
    }
}
