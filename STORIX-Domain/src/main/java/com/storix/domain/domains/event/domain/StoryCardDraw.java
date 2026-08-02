package com.storix.domain.domains.event.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.domain.domains.event.dto.StoryCardLuckyWorkPick;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 뽑은 카드 결과
@Entity
@Getter
@Table(
        name = "event_story_card_draws",
        indexes = @Index(name = "idx_story_card_draw_drawn_on", columnList = "drawn_on"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_story_card_draw_event_user_date",
                columnNames = {"app_event_id", "user_id", "drawn_on"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryCardDraw extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_card_draw_id")
    private Long id;

    @Column(name = "app_event_id", nullable = false)
    private Long appEventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 06:00 기준 서비스 날짜, serviceDateOf(now)
    @Column(name = "drawn_on", nullable = false)
    private LocalDate drawnOn;

    // 배정된 대표 장르. 카드 이미지는 이 값으로 결정된다 (StoryCardGenres)
    @Column(name = "genre", nullable = false, length = 20)
    private Genre genre;

    // 오늘의 한마디. 줄바꿈(\n) 포함 원문
    @Column(name = "message", nullable = false, length = 200)
    private String message;

    // 오늘의 몰입력
    @Column(name = "immersion", nullable = false, length = 100)
    private String immersion;

    @Column(name = "lucky_work_title", nullable = false, length = 100)
    private String luckyWorkTitle;

    @Column(name = "lucky_work_type", nullable = false, length = 20)
    private WorksType luckyWorkType;

    @Enumerated(EnumType.STRING)
    @Column(name = "lucky_work_platform", nullable = false, length = 20)
    private Platform luckyWorkPlatform;

    @Column(name = "lucky_work_landing_url", length = 500)
    private String luckyWorkLandingUrl;

    @Column(name = "drawn_at", nullable = false)
    private LocalDateTime drawnAt;

    @Builder
    public StoryCardDraw(Long appEventId,
                         Long userId,
                         LocalDate drawnOn,
                         Genre genre,
                         String message,
                         String immersion,
                         String luckyWorkTitle,
                         WorksType luckyWorkType,
                         Platform luckyWorkPlatform,
                         String luckyWorkLandingUrl,
                         LocalDateTime drawnAt) {
        this.appEventId = appEventId;
        this.userId = userId;
        this.drawnOn = drawnOn;
        this.genre = genre;
        this.message = message;
        this.immersion = immersion;
        this.luckyWorkTitle = luckyWorkTitle;
        this.luckyWorkType = luckyWorkType;
        this.luckyWorkPlatform = luckyWorkPlatform;
        this.luckyWorkLandingUrl = luckyWorkLandingUrl;
        this.drawnAt = drawnAt;
    }

    // 뽑은 콘텐츠를 그대로 복사해 카드 한 장을 만든다.
    // 작품 정보도 복사해두므로 이후 works 쪽이 바뀌어도 이미 뽑은 카드는 그대로 유지된다
    public static StoryCardDraw of(Long appEventId,
                                   Long userId,
                                   LocalDate drawnOn,
                                   Genre genre,
                                   StoryCardMessage message,
                                   StoryCardImmersion immersion,
                                   StoryCardLuckyWorkPick luckyWork,
                                   LocalDateTime drawnAt) {
        return StoryCardDraw.builder()
                .appEventId(appEventId)
                .userId(userId)
                .drawnOn(drawnOn)
                .genre(genre)
                .message(message.getContent())
                .immersion(immersion.getContent())
                .luckyWorkTitle(luckyWork.title())
                .luckyWorkType(luckyWork.worksType())
                .luckyWorkPlatform(luckyWork.platform())
                .luckyWorkLandingUrl(luckyWork.landingUrl())
                .drawnAt(drawnAt)
                .build();
    }

    // 같은 내용의 카드인지 판별하는 데에 이용
    public boolean isSameCardAs(StoryCardDraw candidate) {
        return candidate != null
                && genre == candidate.genre
                && message.equals(candidate.message)
                && immersion.equals(candidate.immersion)
                && luckyWorkTitle.equals(candidate.luckyWorkTitle);
    }

    // 카드 레이아웃용 줄 배열
    public List<String> messageLines() {
        return List.of(message.split("\n"));
    }

    // 공유/저장 텍스트용 한 줄 표현
    public String messageSingleLine() {
        return message.replace('\n', ' ');
    }

    // "웹툰 ㅣ 화산귀환"
    public String luckyWorkLabel() {
        return luckyWorkType.getDbValue() + " ㅣ " + luckyWorkTitle;
    }
}
