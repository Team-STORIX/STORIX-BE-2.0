package com.storix.domain.domains.works.domain;

import com.storix.common.exception.STORIXDynamicException;
import com.storix.domain.domains.works.dto.WorksMergeCandidate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[작품] 병합 거절 조건")
class WorksMergePolicyTest {

    private static WorksMergeCandidate works(long id, List<Long> reviewerIds, boolean hasTopicRoom) {
        return new WorksMergeCandidate(id, reviewerIds, hasTopicRoom);
    }

    @Test
    @DisplayName("drop 에 keep 이 있거나 drop 끼리 겹치면 거절한다")
    void rejectsInvalidRequest() {
        assertThatThrownBy(() -> WorksMergePolicy.checkRequest(1L, List.of(1L))).isInstanceOf(STORIXDynamicException.class);
        assertThatThrownBy(() -> WorksMergePolicy.checkRequest(1L, List.of(2L, 2L))).isInstanceOf(STORIXDynamicException.class);
        assertThatCode(() -> WorksMergePolicy.checkRequest(1L, List.of(2L, 3L))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("같은 사용자 리뷰가 keep 에 있으면 거절하고 사유에 drop 과 테이블을 적는다")
    void rejectsDuplicatedReviewer() {
        assertThatThrownBy(() -> WorksMergePolicy.checkConflicts(works(1, List.of(10L), false), List.of(works(2, List.of(10L, 11L), false))))
                .isInstanceOf(STORIXDynamicException.class)
                .hasMessage("keep 1 ← drop 2 의 review: 같은 사용자 리뷰가 이미 있습니다 userIds=[10]");
    }

    @Test
    @DisplayName("앞 drop 에서 옮겨 올 리뷰 · 토픽룸까지 포함해 검사한다")
    void checksAcrossDrops() {
        assertThatThrownBy(() -> WorksMergePolicy.checkConflicts(works(1, List.of(), false),
                List.of(works(2, List.of(10L), false), works(3, List.of(10L), false))))
                .hasMessageContaining("drop 3 의 review");
        assertThatThrownBy(() -> WorksMergePolicy.checkConflicts(works(1, List.of(), false),
                List.of(works(2, List.of(), true), works(3, List.of(), true))))
                .hasMessageContaining("drop 3 의 topic_room");
    }

    @Test
    @DisplayName("겹치는 리뷰 · 토픽룸이 없으면 통과한다")
    void passes() {
        assertThatCode(() -> WorksMergePolicy.checkConflicts(works(1, List.of(10L), false),
                List.of(works(2, List.of(11L), true), works(3, List.of(12L), false))))
                .doesNotThrowAnyException();
    }
}
