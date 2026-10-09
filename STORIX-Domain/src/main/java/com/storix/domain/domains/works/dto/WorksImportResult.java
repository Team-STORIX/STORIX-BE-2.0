package com.storix.domain.domains.works.dto;

public record WorksImportResult(
        Long stagingId,
        Status result,
        Long worksId,
        String error
) {

    public enum Status { CREATED, UPDATED, UNCHANGED, FAILED }

    public static WorksImportResult of(Long stagingId, Status status, Long worksId) {
        return new WorksImportResult(stagingId, status, worksId, null);
    }

    public static WorksImportResult failed(Long stagingId, String error) {
        return new WorksImportResult(stagingId, Status.FAILED, null, error);
    }

    public boolean changed() {
        return result == Status.CREATED || result == Status.UPDATED;
    }
}
