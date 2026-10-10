package com.storix.domain.domains.works.dto;

import java.util.List;

public record WorksMoveCount(
        int moved,
        int removedDuplicates,
        List<Long> duplicatedUserIds
) {

    public WorksMoveCount(int moved, int removedDuplicates) {
        this(moved, removedDuplicates, List.of());
    }
}
