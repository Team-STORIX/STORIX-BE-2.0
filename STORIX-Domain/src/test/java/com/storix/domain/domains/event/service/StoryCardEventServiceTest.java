package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.StoryCardContentAdaptor;
import com.storix.domain.domains.event.adaptor.StoryCardDrawAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.StoryCardDraw;
import com.storix.domain.domains.event.domain.StoryCardGenres;
import com.storix.domain.domains.event.domain.StoryCardImmersion;
import com.storix.domain.domains.event.domain.StoryCardLuckyWork;
import com.storix.domain.domains.event.domain.StoryCardMessage;
import com.storix.domain.domains.event.dto.StoryCardResponse;
import com.storix.domain.domains.event.dto.StoryCardStatusResponse;
import com.storix.domain.domains.event.exception.StoryCardEventNotActiveException;
import com.storix.domain.domains.event.exception.StoryCardEventNotFoundException;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[오늘의 스토리 카드] 도메인 서비스 - 현황 조회/카드 뽑기")
class StoryCardEventServiceTest {

    private static final Long EVENT_ID = 200L;
    private static final Long USER_ID = 7L;
    private static final String MESSAGE = "오늘은 대현자처럼\n아무 말 없이 고개를 끄덕여보세요!";
    private static final String IMMERSION = "한 챕터만 읽으려다 밤을 새울 몰입력";
    private static final String LUCKY_WORK_URL = "https://comic.naver.com/webtoon/list?titleId=769209";
    private static final LocalDate START = LocalDate.of(2026, 7, 20);
    private static final LocalDate END = LocalDate.of(2026, 8, 20);

    @Mock
    private AppEventAdaptor appEventAdaptor;

    @Mock
    private StoryCardDrawAdaptor storyCardDrawAdaptor;

    @Mock
    private StoryCardContentAdaptor storyCardContentAdaptor;

    @InjectMocks
    private StoryCardEventService storyCardEventService;

    private AppEvent event(LocalDateTime startAt, LocalDateTime endAt) {
        AppEvent e = AppEvent.builder()
                .name("오늘의 스토리 카드").description("설명")
                .startAt(startAt).endAt(endAt)
                .hasWinner(false)
                .build();
        ReflectionTestUtils.setField(e, "id", EVENT_ID);
        return e;
    }

    private AppEvent defaultEvent() {
        return event(START.atStartOfDay(), END.atTime(23, 59));
    }

    private StoryCardMessage message(Genre genre) {
        return StoryCardMessage.builder().genre(genre).content(MESSAGE).active(true).build();
    }

    private StoryCardImmersion immersion() {
        return StoryCardImmersion.builder().content(IMMERSION).active(true).build();
    }

    private StoryCardLuckyWork luckyWork(Genre genre) {
        return StoryCardLuckyWork.builder()
                .genre(genre).worksType(WorksType.WEBTOON).title("화산귀환")
                .platform(Platform.NAVER_WEBTOON).landingUrl(LUCKY_WORK_URL).active(true)
                .build();
    }

    // 이미 뽑아둔 카드 (콘텐츠 값이 행에 복사돼 있는 상태)
    private StoryCardDraw draw(Genre genre, LocalDate drawnOn) {
        return StoryCardDraw.of(EVENT_ID, USER_ID, drawnOn, genre,
                message(genre), immersion(), luckyWork(genre), drawnOn.atTime(9, 0));
    }

    // 새 카드 뽑기에 필요한 콘텐츠 추출을 한 번에 스텁한다
    private void givenPickedContent() {
        given(storyCardContentAdaptor.pickMessage(any(Genre.class)))
                .willAnswer(inv -> message(inv.getArgument(0)));
        given(storyCardContentAdaptor.pickImmersion()).willReturn(immersion());
        given(storyCardContentAdaptor.pickLuckyWork(any(Genre.class)))
                .willAnswer(inv -> luckyWork(inv.getArgument(0)));
        given(storyCardDrawAdaptor.saveIfAbsent(any(StoryCardDraw.class)))
                .willAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("serviceDateOf - 06:00 초기화 기준일")
    class ServiceDate {

        @Test
        @DisplayName("06:00 이전은 전날 카드, 06:00부터는 당일 카드로 취급한다")
        void reset_hour_boundary() {
            LocalDate day = LocalDate.of(2026, 7, 28);

            assertThat(StoryCardDraw.serviceDateOf(day.atTime(5, 59))).isEqualTo(day.minusDays(1));
            assertThat(StoryCardDraw.serviceDateOf(day.atTime(6, 0))).isEqualTo(day);
            assertThat(StoryCardDraw.serviceDateOf(day.atTime(23, 59))).isEqualTo(day);
            assertThat(StoryCardDraw.serviceDateOf(day.atStartOfDay())).isEqualTo(day.minusDays(1));
        }
    }

    @Nested
    @DisplayName("getStatus - 카드 현황 조회")
    class GetStatus {

        @Test
        @DisplayName("아직 안 뽑았으면 card는 null이고 drawnToday=false다")
        void status_not_drawn() {
            LocalDateTime now = START.plusDays(1).atTime(10, 0);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, START.plusDays(1)))
                    .willReturn(Optional.empty());

            StoryCardStatusResponse status = storyCardEventService.getStatus(EVENT_ID, USER_ID, now);

            assertThat(status.appEventId()).isEqualTo(EVENT_ID);
            assertThat(status.eventStartDate()).isEqualTo(START);
            assertThat(status.eventEndDate()).isEqualTo(END);
            assertThat(status.serviceDate()).isEqualTo(START.plusDays(1));
            assertThat(status.eventActive()).isTrue();
            assertThat(status.drawnToday()).isFalse();
            assertThat(status.card()).isNull();
        }

        @Test
        @DisplayName("이미 뽑았으면 콘텐츠 테이블을 다시 읽지 않고 저장된 행만으로 카드를 만든다")
        void status_already_drawn() {
            LocalDate serviceDate = START.plusDays(1);
            LocalDateTime now = serviceDate.atTime(10, 0);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, serviceDate))
                    .willReturn(Optional.of(draw(Genre.FANTASY, serviceDate)));

            StoryCardStatusResponse status = storyCardEventService.getStatus(EVENT_ID, USER_ID, now);

            assertThat(status.drawnToday()).isTrue();
            assertThat(status.card()).isNotNull();
            assertThat(status.card().alreadyDrawn()).isTrue();
            assertThat(status.card().genre()).isEqualTo("판타지");
            assertThat(status.card().messageLines()).hasSize(2);
            assertThat(status.card().message()).doesNotContain("\n");
            assertThat(status.card().immersion()).isEqualTo(IMMERSION);
            assertThat(status.card().luckyWork().displayLabel()).isEqualTo("웹툰 ㅣ 화산귀환");
            assertThat(status.card().luckyWork().landingUrl()).isEqualTo(LUCKY_WORK_URL);
            verify(storyCardContentAdaptor, never()).pickMessage(any(Genre.class));
        }

        @Test
        @DisplayName("06:00 이전 조회는 전날 카드를 기준으로 찾는다")
        void status_before_reset_hour_uses_previous_day() {
            LocalDate serviceDate = START.plusDays(1);
            LocalDateTime beforeReset = serviceDate.plusDays(1).atTime(5, 30);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, serviceDate))
                    .willReturn(Optional.empty());

            StoryCardStatusResponse status = storyCardEventService.getStatus(EVENT_ID, USER_ID, beforeReset);

            assertThat(status.serviceDate()).isEqualTo(serviceDate);
        }

        @Test
        @DisplayName("이벤트 기간 외에는 eventActive=false로 반환한다")
        void status_inactive_after_end() {
            LocalDateTime now = END.plusDays(2).atTime(10, 0);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, END.plusDays(2)))
                    .willReturn(Optional.empty());

            StoryCardStatusResponse status = storyCardEventService.getStatus(EVENT_ID, USER_ID, now);

            assertThat(status.eventActive()).isFalse();
        }

        @Test
        @DisplayName("이벤트가 설정되지 않았거나(0) 존재하지 않으면 404를 던진다")
        void status_event_not_found() {
            assertThatThrownBy(() -> storyCardEventService.getStatus(0L, USER_ID, START.atTime(10, 0)))
                    .isInstanceOf(StoryCardEventNotFoundException.class);

            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.empty());
            assertThatThrownBy(() -> storyCardEventService.getStatus(EVENT_ID, USER_ID, START.atTime(10, 0)))
                    .isInstanceOf(StoryCardEventNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("draw - 카드 뽑기")
    class Draw {

        @Test
        @DisplayName("이미지/한마디/작품이 모두 같은 장르로 배정된다")
        void draw_uses_single_genre() {
            LocalDate serviceDate = START.plusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, serviceDate)).willReturn(Optional.empty());
            givenPickedContent();

            ArgumentCaptor<Genre> messageGenre = ArgumentCaptor.forClass(Genre.class);
            ArgumentCaptor<Genre> luckyWorkGenre = ArgumentCaptor.forClass(Genre.class);
            ArgumentCaptor<StoryCardDraw> saved = ArgumentCaptor.forClass(StoryCardDraw.class);

            storyCardEventService.draw(EVENT_ID, USER_ID, serviceDate.atTime(10, 0));

            verify(storyCardContentAdaptor).pickMessage(messageGenre.capture());
            verify(storyCardContentAdaptor).pickLuckyWork(luckyWorkGenre.capture());
            verify(storyCardDrawAdaptor).saveIfAbsent(saved.capture());

            Genre assigned = saved.getValue().getGenre();
            assertThat(messageGenre.getValue()).isEqualTo(assigned);
            assertThat(luckyWorkGenre.getValue()).isEqualTo(assigned);
            assertThat(StoryCardGenres.isSupported(assigned)).isTrue();
        }

        @Test
        @DisplayName("뽑은 콘텐츠가 draw 행에 그대로 복사되어 저장된다")
        void draw_snapshots_content() {
            LocalDate serviceDate = START.plusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, serviceDate)).willReturn(Optional.empty());
            givenPickedContent();

            ArgumentCaptor<StoryCardDraw> saved = ArgumentCaptor.forClass(StoryCardDraw.class);

            storyCardEventService.draw(EVENT_ID, USER_ID, serviceDate.atTime(10, 0));

            verify(storyCardDrawAdaptor).saveIfAbsent(saved.capture());
            StoryCardDraw row = saved.getValue();
            assertThat(row.getMessage()).isEqualTo(MESSAGE);
            assertThat(row.getImmersion()).isEqualTo(IMMERSION);
            assertThat(row.getLuckyWorkTitle()).isEqualTo("화산귀환");
            assertThat(row.getLuckyWorkType()).isEqualTo(WorksType.WEBTOON);
            assertThat(row.getLuckyWorkPlatform()).isEqualTo(Platform.NAVER_WEBTOON);
            assertThat(row.getLuckyWorkLandingUrl()).isEqualTo(LUCKY_WORK_URL);
        }

        @Test
        @DisplayName("뽑은 카드는 서비스 날짜로 저장되고 alreadyDrawn=false로 반환된다")
        void draw_ok() {
            LocalDate serviceDate = START.plusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, serviceDate)).willReturn(Optional.empty());
            givenPickedContent();

            StoryCardResponse card = storyCardEventService.draw(EVENT_ID, USER_ID, serviceDate.atTime(10, 0));

            assertThat(card.drawnOn()).isEqualTo(serviceDate);
            assertThat(card.alreadyDrawn()).isFalse();
            assertThat(card.immersion()).isEqualTo(IMMERSION);
            assertThat(card.messageLines()).containsExactly("오늘은 대현자처럼", "아무 말 없이 고개를 끄덕여보세요!");
            assertThat(card.imageUrl()).startsWith("public/event/story-card/");
        }

        @Test
        @DisplayName("같은 날 재요청하면 새로 뽑지 않고 처음 카드를 그대로 반환한다")
        void draw_same_day_returns_existing() {
            LocalDate serviceDate = START.plusDays(1);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));
            given(storyCardDrawAdaptor.findTodayDraw(EVENT_ID, USER_ID, serviceDate))
                    .willReturn(Optional.of(draw(Genre.HISTORICAL, serviceDate)));

            StoryCardResponse card = storyCardEventService.draw(EVENT_ID, USER_ID, serviceDate.atTime(20, 0));

            assertThat(card.alreadyDrawn()).isTrue();
            assertThat(card.genre()).isEqualTo("무협");
            verify(storyCardDrawAdaptor, never()).saveIfAbsent(any(StoryCardDraw.class));
            verify(storyCardContentAdaptor, never()).pickMessage(any(Genre.class));
        }

        @Test
        @DisplayName("이벤트 기간 외 뽑기 요청은 400을 던지고 아무것도 저장하지 않는다")
        void draw_not_active() {
            LocalDateTime beforeStart = START.minusDays(1).atTime(10, 0);
            given(appEventAdaptor.findOptionalById(EVENT_ID)).willReturn(Optional.of(defaultEvent()));

            assertThatThrownBy(() -> storyCardEventService.draw(EVENT_ID, USER_ID, beforeStart))
                    .isInstanceOf(StoryCardEventNotActiveException.class);
            verify(storyCardDrawAdaptor, never()).saveIfAbsent(any(StoryCardDraw.class));
        }

        @Test
        @DisplayName("이벤트가 설정되지 않았으면(0) 404를 던진다")
        void draw_event_not_found() {
            assertThatThrownBy(() -> storyCardEventService.draw(0L, USER_ID, START.atTime(10, 0)))
                    .isInstanceOf(StoryCardEventNotFoundException.class);
        }
    }
}
