package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.dto.WorksImportResult.Status;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record WorksImportOutcome(
        WorksImportResult result,
        String worksName,
        String artistName,
        List<String> changes
) {

    public static WorksImportOutcome created(WorksImportCommand command, Long worksId) {
        return new WorksImportOutcome(
                WorksImportResult.of(command.item().stagingId(), Status.CREATED, worksId),
                command.worksName(),
                command.artistName(),
                List.of()
        );
    }

    public static WorksImportOutcome updated(WorksImportCommand command, Long worksId, List<String> changes) {
        return new WorksImportOutcome(
                WorksImportResult.of(command.item().stagingId(), changes.isEmpty() ? Status.UNCHANGED : Status.UPDATED, worksId),
                command.worksName(),
                command.artistName(),
                changes
        );
    }

    public static WorksImportOutcome notCreated(WorksImportCommand command, Status status, List<Long> candidateWorksIds) {
        return new WorksImportOutcome(
                WorksImportResult.notCreated(command.item().stagingId(), status, candidateWorksIds),
                command.worksName(),
                command.artistName(),
                List.of()
        );
    }

    public static WorksImportOutcome failed(WorksImportItem item, String error) {
        return new WorksImportOutcome(
                WorksImportResult.failed(item.stagingId(), error),
                item.worksName(),
                item.artistName(),
                List.of()
        );
    }

    public static Map<Status, Long> countByStatus(List<WorksImportOutcome> outcomes) {
        Map<Status, Long> counts = new EnumMap<>(Status.class);
        outcomes.forEach(outcome -> counts.merge(outcome.result().result(), 1L, Long::sum));
        return counts;
    }

    public Map<String, Object> toLogItem() {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("stagingId", result.stagingId());
        item.put("result", result.result());
        if (result.worksId() != null) item.put("worksId", result.worksId());
        if (result.candidateWorksIds() != null) item.put("candidateWorksIds", result.candidateWorksIds());
        item.put("worksName", worksName);
        item.put("artistName", artistName);
        if (!changes.isEmpty()) item.put("changes", changes);
        if (result.error() != null) item.put("error", result.error());
        return item;
    }
}
