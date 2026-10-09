package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksImportResult(
        Long stagingId,
        Status result,
        Long worksId,
        List<Long> candidateWorksIds,
        String error
) {

    // SUSPECTED_DUPLICATE: 중복 의심, SKIPPED: 웹소설이 있는 단행본
    public enum Status { CREATED, UPDATED, UNCHANGED, SUSPECTED_DUPLICATE, SKIPPED, FAILED }

    public static WorksImportResult of(Long stagingId, Status status, Long worksId) {
        return new WorksImportResult(stagingId, status, worksId, null, null);
    }

    public static WorksImportResult notCreated(Long stagingId, Status status, List<Long> candidateWorksIds) {
        return new WorksImportResult(stagingId, status, null, candidateWorksIds, null);
    }

    public static WorksImportResult failed(Long stagingId, String error) {
        return new WorksImportResult(stagingId, Status.FAILED, null, null, error);
    }

    public boolean changed() {
        return result == Status.CREATED || result == Status.UPDATED;
    }

    public static List<Long> changedWorksIds(List<WorksImportResult> results) {
        return results.stream()
                .filter(WorksImportResult::changed)
                .map(WorksImportResult::worksId)
                .distinct()
                .toList();
    }
}
