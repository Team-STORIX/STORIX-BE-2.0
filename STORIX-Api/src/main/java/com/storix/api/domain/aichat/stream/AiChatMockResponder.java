package com.storix.api.domain.aichat.stream;

import com.storix.api.domain.aichat.controller.dto.AiChatMatchedClue;
import com.storix.api.domain.aichat.stream.dto.AiChatMockPick;
import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import com.storix.domain.domains.works.service.WorksCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Profile("!prod")
@ConditionalOnProperty(name = "ai.chat.responder", havingValue = "mock")
@RequiredArgsConstructor
public class AiChatMockResponder implements AiChatResponder {

    private static final String PIPELINE_VERSION = "mock";
    private static final long STEP_DELAY_MILLIS = 600L;
    private static final long TOKEN_DELAY_MILLIS = 80L;
    private static final int MAX_WORKS = 5;

    private static final List<AiChatMockPick> PICKS = List.of(
            new AiChatMockPick("치즈인더트랩", WorksType.WEBTOON),
            new AiChatMockPick("어두운 바다의 등불이 되어", WorksType.WEBNOVEL),
            new AiChatMockPick("전지적 독자 시점", WorksType.WEBTOON),
            new AiChatMockPick("블랙 스완", WorksType.WEBTOON)
    );

    private final WorksCardService worksCardService;

    // 질문에 "오류" · "작품외" · "없는작품" · "결과없음" · "1개" 가 있으면 해당 응답, 그 밖에는 DB 작품 카드 5개
    // 19금(블랙 스완)은 19금 추천을 켠 성인 인증 사용자에게만
    @Override
    public void respond(AiChatQuestion question, AiChatAnswerSink sink) throws InterruptedException {
        String text = question.question();
        sink.progress("SEARCHING", "작품 찾는 중…");

        if (text.contains("오류")) {
            Thread.sleep(STEP_DELAY_MILLIS);
            sink.error("TIMEOUT", "일시적인 오류로 검색하지 못했어요. 잠시 후 다시 시도해 주세요.");
            return;
        }
        if (text.contains("작품외")) {
            answer(sink, AiChatAnswerStatus.OFF_TOPIC, List.of("저는 작품 찾는 걸 도와드려요. ", "찾고 싶은 작품을 말해 주세요!"), List.of());
            return;
        }
        if (text.contains("없는작품")) {
            answer(sink, AiChatAnswerStatus.WORK_NOT_FOUND, List.of("아직 스토릭스에 없는 작품이에요. ", "작품 추가를 요청해 주시면 확인해 볼게요!"), List.of());
            return;
        }
        if (text.contains("결과없음")) {
            answer(sink, AiChatAnswerStatus.NO_RESULT, List.of("기억나는 표지나 줄거리 단서가 있을까요? ", "주인공 외형이나 읽었던 플랫폼도 검색에 도움이 돼요."), List.of());
            return;
        }

        int count = text.contains("1개") ? 1 : MAX_WORKS;
        List<AiChatRawWork> works = pickWorks(question.adultAllowed(), count);
        Thread.sleep(STEP_DELAY_MILLIS);
        sink.progress("FOUND", "후보 20개 찾음");
        List<String> tokens = count == 1
                ? List.of("단서가 비슷한 작품 ", "1개를 찾았어요.")
                : List.of("조건에 가까운 작품부터 ", "보여 드릴게요.");
        answer(sink, AiChatAnswerStatus.OK, tokens, works);
    }

    private void answer(AiChatAnswerSink sink, AiChatAnswerStatus status, List<String> tokens,
                        List<AiChatRawWork> works) throws InterruptedException {
        for (String token : tokens) {
            Thread.sleep(TOKEN_DELAY_MILLIS);
            sink.token(token);
        }
        Thread.sleep(TOKEN_DELAY_MILLIS);
        sink.works(works, PIPELINE_VERSION);
        sink.done(status);
    }

    // 정해 둔 작품을 먼저, 모자라면 DB 에서 무작위로 채운다. 19금은 실제와 같은 규칙으로 거른다
    private List<Long> chooseIds(boolean adultAllowed, int count) {
        List<Long> pickIds = PICKS.stream()
                .map(pick -> worksCardService.findIdByTitle(pick.title(), pick.worksType()))
                .flatMap(Optional::stream)
                .toList();
        Map<Long, WorksCardInfo> pickCards = worksCardService.findCards(pickIds);
        List<Long> chosen = new ArrayList<>(pickIds.stream()
                .filter(id -> pickCards.containsKey(id) && (adultAllowed || !pickCards.get(id).adult()))
                .limit(count)
                .toList());
        if (chosen.size() < count) {
            chosen.addAll(worksCardService.pickRandomIds(chosen, count - chosen.size(), !adultAllowed));
        }
        return chosen;
    }

    private List<AiChatRawWork> pickWorks(boolean adultAllowed, int count) {
        List<Long> ids = chooseIds(adultAllowed, count);
        Map<Long, WorksCardInfo> cards = worksCardService.findCards(ids);
        List<AiChatRawWork> works = new ArrayList<>();
        for (Long id : ids) {
            WorksCardInfo card = cards.get(id);
            if (card == null) {
                continue;
            }
            List<AiChatMatchedClue> clues = card.genre() == null
                    ? List.of(new AiChatMatchedClue("질문과 비슷한 분위기", "DESCRIPTION"))
                    : List.of(new AiChatMatchedClue(card.genre().getDbValue(), "GENRE"),
                            new AiChatMatchedClue("질문과 비슷한 분위기", "DESCRIPTION"));
            works.add(new AiChatRawWork(works.size() + 1, id, "질문에 적은 조건과 가까운 작품이에요.", clues));
        }
        return works;
    }
}
