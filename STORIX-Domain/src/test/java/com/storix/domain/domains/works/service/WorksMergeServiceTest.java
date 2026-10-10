package com.storix.domain.domains.works.service;

import com.storix.common.exception.STORIXDynamicException;
import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.preference.adaptor.ExplorationAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksMergeResult;
import com.storix.domain.domains.works.dto.WorksMoveCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[작품] 병합")
class WorksMergeServiceTest {

    private static final Long KEEP = 1L;
    private static final Long DROP = 2L;

    @Mock private WorksAdaptor worksAdaptor;
    @Mock private FavoriteWorksAdaptor favoriteWorksAdaptor;
    @Mock private GenreScoreAdaptor genreScoreAdaptor;
    @Mock private ExplorationAdaptor explorationAdaptor;
    @Mock private ReviewAdaptor reviewAdaptor;
    @Mock private BoardAdaptor boardAdaptor;
    @Mock private TopicRoomAdaptor topicRoomAdaptor;
    @InjectMocks private WorksMergeService worksMergeService;

    private Works works(String description, String thumbnailUrl) {
        return Works.builder()
                .worksName("천관사복")
                .artistName("묵향동후")
                .ageClassification(AgeClassification.AGE_15)
                .genre(Genre.FANTASY)
                .worksType(WorksType.WEBNOVEL)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    @Test
    @DisplayName("keep 과 같은 id 가 drop 에 있으면 거절한다")
    void rejectsKeepInDrops() {
        assertThatThrownBy(() -> worksMergeService.merge(KEEP, List.of(KEEP)))
                .isInstanceOf(STORIXDynamicException.class);
    }

    @Test
    @DisplayName("같은 사용자 리뷰가 양쪽에 있으면 아무것도 옮기지 않고 거절한다")
    void rejectsDuplicatedReviewer() {
        given(reviewAdaptor.findLibraryUserIdsByWorksId(KEEP)).willReturn(List.of(10L));
        given(reviewAdaptor.findLibraryUserIdsByWorksId(DROP)).willReturn(List.of(10L, 11L));

        assertThatThrownBy(() -> worksMergeService.merge(KEEP, List.of(DROP)))
                .isInstanceOf(STORIXDynamicException.class)
                .hasMessageContaining("drop 2 의 review")
                .hasMessageContaining("10");
        verify(reviewAdaptor, never()).moveWorks(anyLong(), anyLong());
    }

    @Test
    @DisplayName("keep 과 drop 모두 토픽룸이 있으면 거절한다")
    void rejectsBothTopicRooms() {
        given(topicRoomAdaptor.existsByWorksId(KEEP)).willReturn(true);
        given(topicRoomAdaptor.existsByWorksId(DROP)).willReturn(true);

        assertThatThrownBy(() -> worksMergeService.merge(KEEP, List.of(DROP)))
                .isInstanceOf(STORIXDynamicException.class)
                .hasMessageContaining("drop 2 의 topic_room");
        verify(topicRoomAdaptor, never()).moveWorks(anyLong(), anyLong());
    }

    @Test
    @DisplayName("참조를 옮기고 keep 의 빈 값 · 플랫폼을 채운 뒤 drop 을 지우고 리뷰 통계를 다시 계산한다")
    void mergesIntoKeep() {
        Works keep = works("keep 소개", " ");
        Works drop = works("drop 소개", "https://drop.png");
        drop.addPlatform(Platform.NAVER_SERIES, "https://series/1");
        given(worksAdaptor.findById(KEEP)).willReturn(keep);
        given(worksAdaptor.findById(DROP)).willReturn(drop);
        given(worksAdaptor.moveNicknames(DROP, KEEP)).willReturn(new WorksMoveCount(1, 1));
        given(favoriteWorksAdaptor.moveWorks(DROP, KEEP)).willReturn(new WorksMoveCount(3, 2, List.of(12L, 13L)));
        given(genreScoreAdaptor.moveScoreLogs(DROP, KEEP, List.of(12L, 13L))).willReturn(new WorksMoveCount(4, 1));
        given(explorationAdaptor.moveWorks(DROP, KEEP)).willReturn(new WorksMoveCount(0, 0));
        given(reviewAdaptor.moveWorks(DROP, KEEP)).willReturn(1);
        given(reviewAdaptor.findActiveRatingsByWorksId(KEEP)).willReturn(List.of(Rating.FIVE, Rating.FOUR));

        WorksMergeResult result = worksMergeService.merge(KEEP, List.of(DROP));

        assertThat(keep.getDescription()).isEqualTo("keep 소개");
        assertThat(keep.getThumbnailUrl()).isEqualTo("https://drop.png");
        assertThat(keep.getPlatforms()).extracting("platform").containsExactly(Platform.NAVER_SERIES);
        assertThat(result.moved()).containsEntry("user_favorite_works", 3).containsEntry("review", 1);
        assertThat(result.removedDuplicates()).containsEntry("user_favorite_works", 2).containsEntry("works_nickname", 1)
                .containsEntry("user_genre_score_log", 1);
        assertThat(result.reviewsCount()).isEqualTo(2);
        assertThat(result.avgRating()).isEqualTo(4.5);
        verify(worksAdaptor).delete(drop);
    }
}
