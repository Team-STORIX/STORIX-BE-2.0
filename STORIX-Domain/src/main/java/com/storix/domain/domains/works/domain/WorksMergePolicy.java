package com.storix.domain.domains.works.domain;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXDynamicException;
import com.storix.domain.domains.works.dto.WorksMergeCandidate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WorksMergePolicy {

    private WorksMergePolicy() {
    }

    public static void checkRequest(Long keepWorksId, List<Long> dropWorksIds) {
        if (dropWorksIds.contains(keepWorksId) || new HashSet<>(dropWorksIds).size() != dropWorksIds.size()) {
            throw new STORIXDynamicException(ErrorCode.WORKS_MERGE_INVALID_REQUEST,
                    "keep " + keepWorksId + " 과 같거나 겹치는 drop id 가 있습니다: " + dropWorksIds, null);
        }
    }

    // drop 을 순서대로 keep 에 합친다고 보고 앞 drop 에서 옮겨 온 것까지 포함해 검사
    public static void checkConflicts(WorksMergeCandidate keep, List<WorksMergeCandidate> drops) {
        Set<Long> reviewerIds = new HashSet<>(keep.reviewerIds());
        boolean hasTopicRoom = keep.hasTopicRoom();

        for (WorksMergeCandidate drop : drops) {
            // 리뷰는 유저 · 작품당 하나
            List<Long> duplicated = drop.reviewerIds().stream().filter(reviewerIds::contains).toList();
            if (!duplicated.isEmpty()) {
                throw conflict(keep, drop, WorksMergeTarget.REVIEW, "같은 사용자 리뷰가 이미 있습니다 userIds=" + duplicated);
            }
            reviewerIds.addAll(drop.reviewerIds());

            // 토픽룸은 작품당 하나, 채팅방은 합치지 않음
            if (drop.hasTopicRoom() && hasTopicRoom) {
                throw conflict(keep, drop, WorksMergeTarget.TOPIC_ROOM, "토픽룸이 이미 있습니다");
            }
            hasTopicRoom |= drop.hasTopicRoom();
        }
    }

    private static STORIXDynamicException conflict(WorksMergeCandidate keep, WorksMergeCandidate drop,
                                                   WorksMergeTarget target, String reason) {
        return new STORIXDynamicException(ErrorCode.WORKS_MERGE_CONFLICT,
                "keep " + keep.worksId() + " ← drop " + drop.worksId() + " 의 " + target.tableName() + ": " + reason, null);
    }
}
