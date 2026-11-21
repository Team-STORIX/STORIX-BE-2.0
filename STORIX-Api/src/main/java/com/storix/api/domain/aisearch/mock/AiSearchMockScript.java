package com.storix.api.domain.aisearch.mock;

import com.storix.api.domain.aisearch.controller.dto.AiSearchDoneEvent;
import com.storix.api.domain.aisearch.controller.dto.AiSearchErrorEvent;
import com.storix.api.domain.aisearch.controller.dto.AiSearchMatchedClue;
import com.storix.api.domain.aisearch.controller.dto.AiSearchProgressEvent;
import com.storix.api.domain.aisearch.controller.dto.AiSearchRecommendedWork;
import com.storix.api.domain.aisearch.controller.dto.AiSearchTokenEvent;
import com.storix.api.domain.aisearch.controller.dto.AiSearchWorksEvent;

import java.util.ArrayList;
import java.util.List;

public final class AiSearchMockScript {

    private static final String PIPELINE_VERSION = "mock";
    private static final long MOCK_MESSAGE_ID = 1L;
    private static final long TOKEN_DELAY_MILLIS = 80L;
    private static final long STEP_DELAY_MILLIS = 600L;

    private AiSearchMockScript() {
    }

    public static List<AiSearchMockEvent> of(Long chatId, String question) {
        if (question.contains("오류")) {
            return List.of(
                    new AiSearchMockEvent("progress", new AiSearchProgressEvent("SEARCHING", "작품 찾는 중…"), 0L),
                    new AiSearchMockEvent("error", new AiSearchErrorEvent("TIMEOUT", "잠시 후 다시 시도해 주세요."), STEP_DELAY_MILLIS));
        }
        if (question.contains("작품외")) {
            return answer(chatId, "OFF_TOPIC", List.of("저는 작품 찾는 걸 도와드려요. ", "찾고 싶은 작품을 말해 주세요!"), List.of());
        }
        if (question.contains("없는작품")) {
            return answer(chatId, "WORK_NOT_FOUND", List.of("아직 스토릭스에 없는 작품이에요. ", "작품 추가를 요청해 주시면 확인해 볼게요!"), List.of());
        }
        if (question.contains("결과없음")) {
            return answer(chatId, "NO_RESULT", List.of("딱 맞는 작품을 못 찾았어요. ", "장르나 분위기를 조금 더 알려 주시겠어요?"), List.of());
        }
        return answer(chatId, "OK", List.of("은발 꼬마와 ", "흑발 귀족 보호자가 나오는 ", "육아물 위주로 골라 봤어요."), sampleWorks());
    }

    private static List<AiSearchMockEvent> answer(Long chatId, String status, List<String> tokens, List<AiSearchRecommendedWork> works) {
        List<AiSearchMockEvent> events = new ArrayList<>();
        events.add(new AiSearchMockEvent("progress", new AiSearchProgressEvent("SEARCHING", "작품 찾는 중…"), 0L));
        if (!works.isEmpty()) {
            events.add(new AiSearchMockEvent("progress", new AiSearchProgressEvent("FOUND", "후보 12개 찾음"), STEP_DELAY_MILLIS));
        }
        tokens.forEach(text -> events.add(new AiSearchMockEvent("token", new AiSearchTokenEvent(text), TOKEN_DELAY_MILLIS)));
        events.add(new AiSearchMockEvent("works", new AiSearchWorksEvent(works, PIPELINE_VERSION), TOKEN_DELAY_MILLIS));
        events.add(new AiSearchMockEvent("done", new AiSearchDoneEvent(chatId, MOCK_MESSAGE_ID, status), TOKEN_DELAY_MILLIS));
        return events;
    }

    private static List<AiSearchRecommendedWork> sampleWorks() {
        return List.of(
                new AiSearchRecommendedWork(1, 1L, "목업 작품 1", null, "카카오페이지",
                        "은발 유아 주인공과 귀족 보호자가 나오는 육아물이에요.",
                        List.of(new AiSearchMatchedClue("은발 보라 눈 유아", "COVER"), new AiSearchMatchedClue("육아물", "TAG"))),
                new AiSearchRecommendedWork(2, 2L, "목업 작품 2", null, "네이버 웹툰",
                        "입양된 막내가 가족의 사랑을 받는 이야기예요.",
                        List.of(new AiSearchMatchedClue("입양 · 가족물", "DESCRIPTION"))),
                new AiSearchRecommendedWork(3, 3L, "목업 작품 3", null, "리디",
                        "냉정한 공작이 아이를 맡게 되는 로판이에요.",
                        List.of(new AiSearchMatchedClue("공작 보호자", "DESCRIPTION"))));
    }
}
