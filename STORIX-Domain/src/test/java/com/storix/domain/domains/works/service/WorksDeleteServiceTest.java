package com.storix.domain.domains.works.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.preference.adaptor.ExplorationAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksDeleteItem;
import com.storix.domain.domains.works.dto.WorksDeleteItem.Status;
import com.storix.domain.domains.works.dto.WorksDeleteResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[작품] 삭제")
class WorksDeleteServiceTest {

    private static final Long WORKS_ID = 1817L;
    private static final Long BLOCKED_ID = 2532L;

    @Mock private WorksAdaptor worksAdaptor;
    @Mock private FavoriteWorksAdaptor favoriteWorksAdaptor;
    @Mock private GenreScoreAdaptor genreScoreAdaptor;
    @Mock private ExplorationAdaptor explorationAdaptor;
    @Mock private ReviewAdaptor reviewAdaptor;
    @Mock private BoardAdaptor boardAdaptor;
    @Mock private TopicRoomAdaptor topicRoomAdaptor;
    @InjectMocks private WorksDeleteService worksDeleteService;

    private Works works(String name) {
        return Works.builder()
                .worksName(name)
                .artistName("작가")
                .ageClassification(AgeClassification.AGE_15)
                .genre(Genre.ROMANCE)
                .worksType(WorksType.WEBTOON)
                .build();
    }

    @Test
    @DisplayName("dryRun 이면 건수만 세고 아무것도 지우지 않는다")
    void dryRunCountsOnly() {
        given(worksAdaptor.findOptionalById(WORKS_ID)).willReturn(Optional.of(works("주체할 수 없는")));
        given(worksAdaptor.countNicknamesByWorksId(WORKS_ID)).willReturn(3L);

        WorksDeleteItem item = worksDeleteService.deleteAll(List.of(WORKS_ID), true).works().get(0);

        assertThat(item.status()).isEqualTo(Status.DELETABLE);
        assertThat(item.worksName()).isEqualTo("주체할 수 없는");
        assertThat(item.deleted()).containsEntry("works_nickname", 3L).containsEntry("works_platform", 0L);
        assertThat(item.blockedBy()).isEmpty();
        verify(worksAdaptor, never()).delete(any());
        verify(worksAdaptor, never()).deleteNicknamesByWorksId(anyLong());
    }

    @Test
    @DisplayName("사용자 콘텐츠가 있는 작품은 막고 나머지는 지운다")
    void blocksOnlyUserContentWorks() {
        Works works = works("주체할 수 없는");
        given(worksAdaptor.findOptionalById(WORKS_ID)).willReturn(Optional.of(works));
        given(worksAdaptor.findById(WORKS_ID)).willReturn(works);
        given(worksAdaptor.findOptionalById(BLOCKED_ID)).willReturn(Optional.of(works("이터널스")));
        given(boardAdaptor.countReaderBoardsByWorksId(BLOCKED_ID)).willReturn(2L);

        WorksDeleteResult result = worksDeleteService.deleteAll(List.of(BLOCKED_ID, WORKS_ID), false);

        assertThat(result.works()).extracting(WorksDeleteItem::status).containsExactly(Status.BLOCKED, Status.DELETED);
        assertThat(result.works().get(0).blockedBy()).containsExactly(Map.entry("reader_board", 2L));
        assertThat(result.deletedWorksIds()).containsExactly(WORKS_ID);
        verify(worksAdaptor, never()).deleteNicknamesByWorksId(BLOCKED_ID);
        verify(worksAdaptor).deleteNicknamesByWorksId(WORKS_ID);
        verify(explorationAdaptor).deleteAllByWorksId(WORKS_ID);
        verify(worksAdaptor).delete(works);
    }

    @Test
    @DisplayName("관심작품 · 장르 점수 기록이 있어도 막는다")
    void blocksUserActivities() {
        given(worksAdaptor.findOptionalById(WORKS_ID)).willReturn(Optional.of(works("주체할 수 없는")));
        given(favoriteWorksAdaptor.countByWorksId(WORKS_ID)).willReturn(1L);
        given(genreScoreAdaptor.countScoreLogsByWorksId(WORKS_ID)).willReturn(2L);

        WorksDeleteItem item = worksDeleteService.deleteAll(List.of(WORKS_ID), false).works().get(0);

        assertThat(item.status()).isEqualTo(Status.BLOCKED);
        assertThat(item.blockedBy()).containsExactly(
                Map.entry("user_favorite_works", 1L), Map.entry("user_genre_score_log", 2L));
        verify(worksAdaptor, never()).delete(any());
    }

    @Test
    @DisplayName("없는 작품은 NOT_FOUND 로 돌려주고 다음 작품을 계속 처리한다")
    void skipsMissing() {
        Works works = works("주체할 수 없는");
        given(worksAdaptor.findOptionalById(1L)).willReturn(Optional.empty());
        given(worksAdaptor.findOptionalById(WORKS_ID)).willReturn(Optional.of(works));
        given(worksAdaptor.findById(WORKS_ID)).willReturn(works);

        WorksDeleteResult result = worksDeleteService.deleteAll(List.of(1L, WORKS_ID), false);

        assertThat(result.works()).extracting(WorksDeleteItem::status).containsExactly(Status.NOT_FOUND, Status.DELETED);
        assertThat(result.deletedWorksIds()).containsExactly(WORKS_ID);
    }
}
