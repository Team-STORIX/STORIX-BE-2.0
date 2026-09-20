package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.feed.adaptor.TodayFeedSnapshotAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[오늘의 피드] 선정 - 08시 경계 / 스냅샷")
class TodayFeedSelectionHelperTest {

    private static final LocalDate SELECTION_DATE = LocalDate.of(2026, 7, 31);

    @Mock
    private ReaderFeedAdaptor readerFeedAdaptor;

    @Mock
    private TodayFeedSnapshotAdaptor snapshotAdaptor;

    @InjectMocks
    private TodayFeedSelectionHelper todayFeedSelectionHelper;

    @Nested
    @DisplayName("선정일 경계")
    class SelectionDate {

        @Test
        @DisplayName("08시 이전이면 전날이 선정일이다")
        void before_eight_belongs_to_previous_day() {
            LocalDateTime at0759 = LocalDateTime.of(2026, 7, 31, 7, 59, 59);

            assertThat(TodayFeedSelectionHelper.selectionDateOf(at0759))
                    .isEqualTo(LocalDate.of(2026, 7, 30));
        }

        @Test
        @DisplayName("08시 정각부터 당일이 선정일이다")
        void eight_oclock_belongs_to_same_day() {
            LocalDateTime at0800 = LocalDateTime.of(2026, 7, 31, 8, 0, 0);

            assertThat(TodayFeedSelectionHelper.selectionDateOf(at0800))
                    .isEqualTo(LocalDate.of(2026, 7, 31));
        }

        @Test
        @DisplayName("자정 직후에도 선정일이 넘어가지 않는다")
        void midnight_does_not_roll_selection_date() {
            LocalDateTime justAfterMidnight = LocalDateTime.of(2026, 8, 1, 0, 0, 1);

            assertThat(TodayFeedSelectionHelper.selectionDateOf(justAfterMidnight))
                    .isEqualTo(LocalDate.of(2026, 7, 31));
        }
    }

    @Nested
    @DisplayName("스냅샷")
    class Snapshot {

        @Test
        @DisplayName("스냅샷이 있으면 재계산하지 않는다")
        void reuses_existing_snapshot() {
            given(snapshotAdaptor.find(SELECTION_DATE)).willReturn(Optional.of(List.of(7L, 8L, 9L)));

            List<Long> selected = todayFeedSelectionHelper.loadOrSelect(atSelectionTime());

            assertThat(selected).containsExactly(7L, 8L, 9L);
            verify(readerFeedAdaptor, never()).findTodayFeedCandidateIds(any(), any(), anyInt());
        }

        @Test
        @DisplayName("후보가 0건인 스냅샷도 유효한 선정으로 보고 재계산하지 않는다")
        void empty_snapshot_is_not_recalculated() {
            given(snapshotAdaptor.find(SELECTION_DATE)).willReturn(Optional.of(List.of()));

            List<Long> selected = todayFeedSelectionHelper.loadOrSelect(atSelectionTime());

            assertThat(selected).isEmpty();
            verify(readerFeedAdaptor, never()).findTodayFeedCandidateIds(any(), any(), anyInt());
        }

        @Test
        @DisplayName("스냅샷이 없으면 선정 후 저장한다")
        void selects_and_saves_when_absent() {
            given(snapshotAdaptor.find(SELECTION_DATE)).willReturn(Optional.empty());
            givenCandidates(List.of(10L, 11L, 12L));

            List<Long> selected = todayFeedSelectionHelper.loadOrSelect(atSelectionTime());

            assertThat(selected).containsExactly(10L, 11L, 12L);
            verify(snapshotAdaptor).save(SELECTION_DATE, List.of(10L, 11L, 12L));
        }

        @Test
        @DisplayName("후보가 없으면 빈 선정을 저장한다")
        void no_candidates_saves_empty() {
            givenCandidates(List.of());

            List<Long> selected = todayFeedSelectionHelper.reselect(atSelectionTime());

            assertThat(selected).isEmpty();
            verify(snapshotAdaptor).save(SELECTION_DATE, List.of());
        }

        @Test
        @DisplayName("후보 조회 기준 시각은 now 가 아니라 선정일 08시에서 24시간 전이다")
        void window_is_anchored_to_selection_time() {
            givenCandidates(List.of());

            todayFeedSelectionHelper.reselect(LocalDateTime.of(2026, 7, 31, 14, 30));

            ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(readerFeedAdaptor).findTodayFeedCandidateIds(captor.capture(), any(), anyInt());

            assertThat(captor.getValue()).isEqualTo(LocalDateTime.of(2026, 7, 30, 8, 0));
        }
    }

    @Nested
    @DisplayName("동점 정렬 시드")
    class Seed {

        @Test
        @DisplayName("시드는 선정일이라 같은 날 여러 번 계산해도 순서가 같다")
        void seed_is_selection_date() {
            givenCandidates(List.of());

            todayFeedSelectionHelper.reselect(LocalDateTime.of(2026, 7, 31, 8, 0));
            todayFeedSelectionHelper.reselect(LocalDateTime.of(2026, 7, 31, 23, 59));

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(readerFeedAdaptor, times(2))
                    .findTodayFeedCandidateIds(any(), captor.capture(), anyInt());

            assertThat(captor.getAllValues()).containsExactly("20260731", "20260731");
        }

        @Test
        @DisplayName("선정일이 바뀌면 시드도 바뀐다")
        void seed_changes_with_selection_date() {
            givenCandidates(List.of());

            todayFeedSelectionHelper.reselect(LocalDateTime.of(2026, 7, 31, 8, 0));
            todayFeedSelectionHelper.reselect(LocalDateTime.of(2026, 8, 1, 8, 0));

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(readerFeedAdaptor, times(2))
                    .findTodayFeedCandidateIds(any(), captor.capture(), anyInt());

            assertThat(captor.getAllValues()).containsExactly("20260731", "20260801");
        }
    }

    private void givenCandidates(List<Long> boardIds) {
        given(readerFeedAdaptor.findTodayFeedCandidateIds(
                any(LocalDateTime.class), any(String.class), anyInt()))
                .willReturn(boardIds);
    }

    private LocalDateTime atSelectionTime() {
        return LocalDateTime.of(SELECTION_DATE, TodayFeedSelectionHelper.SELECTION_TIME);
    }
}
