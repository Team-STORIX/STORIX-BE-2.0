package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.WorksMergeTarget;
import java.util.LinkedHashMap;
import java.util.Map;

public record WorksMergeCounts(
        Map<String, Integer> moved,
        Map<String, Integer> removedDuplicates
) {

    public static WorksMergeCounts empty() {
        return new WorksMergeCounts(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public void add(WorksMergeTarget target, WorksMoveCount count) {
        moved.merge(target.tableName(), count.moved(), Integer::sum);
        removedDuplicates.merge(target.tableName(), count.removedDuplicates(), Integer::sum);
    }

    public void add(WorksMergeTarget target, int movedCount) {
        moved.merge(target.tableName(), movedCount, Integer::sum);
    }
}
