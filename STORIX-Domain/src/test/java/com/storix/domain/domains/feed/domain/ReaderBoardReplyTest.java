package com.storix.domain.domains.feed.domain;

import com.storix.domain.domains.plus.domain.DeletedBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[피드 댓글] 본인 삭제")
class ReaderBoardReplyTest {

    private ReaderBoardReply newReply() {
        return ReaderBoardReply.builder()
                .userId(10L)
                .comment("원래 댓글")
                .build();
    }

    @Test
    @DisplayName("soft delete 하면 deletedAt 을 채워 하드 삭제 대상이 된다")
    void softDeleteSetsDeletedAt() {
        ReaderBoardReply reply = newReply();

        reply.softDelete();

        assertThat(reply.isDeleted()).isTrue();
        assertThat(reply.getDeletedBy()).isEqualTo(DeletedBy.USER);
        assertThat(reply.getDeletedAt()).isNotNull();
        assertThat(reply.getComment()).isEqualTo("삭제된 댓글입니다");
    }

    @Test
    @DisplayName("이미 지운 댓글은 다시 지워도 deletedAt 이 바뀌지 않는다")
    void softDeleteTwiceKeepsDeletedAt() {
        ReaderBoardReply reply = newReply();
        reply.softDelete();
        LocalDateTime first = reply.getDeletedAt();

        reply.softDelete();

        assertThat(reply.getDeletedAt()).isEqualTo(first);
    }
}
