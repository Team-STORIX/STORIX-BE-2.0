package com.storix.api.domain.aichat.mock;

import com.storix.api.domain.aichat.controller.dto.AiChatDoneEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatErrorEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatMatchedClue;
import com.storix.api.domain.aichat.controller.dto.AiChatProgressEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatRecommendedWork;
import com.storix.api.domain.aichat.controller.dto.AiChatTokenEvent;
import com.storix.api.domain.aichat.controller.dto.AiChatWorksEvent;

import java.util.ArrayList;
import java.util.List;

public final class AiChatMockScript {

    private static final String PIPELINE_VERSION = "mock";
    private static final long MOCK_MESSAGE_ID = 1L;
    private static final long TOKEN_DELAY_MILLIS = 80L;
    private static final long STEP_DELAY_MILLIS = 600L;

    private AiChatMockScript() {
    }

    public static List<AiChatMockEvent> of(String question) {
        if (question.contains("오류")) {
            return List.of(
                    new AiChatMockEvent("progress", new AiChatProgressEvent("SEARCHING", "작품 찾는 중…"), 0L),
                    new AiChatMockEvent("error", new AiChatErrorEvent("TIMEOUT", "잠시 후 다시 시도해 주세요."), STEP_DELAY_MILLIS));
        }
        if (question.contains("작품외")) {
            return answer("OFF_TOPIC", List.of("저는 작품 찾는 걸 도와드려요. ", "찾고 싶은 작품을 말해 주세요!"), List.of());
        }
        if (question.contains("없는작품")) {
            return answer("WORK_NOT_FOUND", List.of("아직 스토릭스에 없는 작품이에요. ", "작품 추가를 요청해 주시면 확인해 볼게요!"), List.of());
        }
        if (question.contains("결과없음")) {
            return answer("NO_RESULT", List.of("딱 맞는 작품을 못 찾았어요. ", "장르나 분위기를 조금 더 알려 주시겠어요?"), List.of());
        }
        if (question.contains("1개")) {
            return answer("OK", List.of("표지 단서가 비슷한 작품 ", "1개를 찾았어요."), sampleWorks().subList(0, 1));
        }
        return answer("OK", List.of("은발 꼬마와 ", "흑발 귀족 보호자가 나오는 ", "육아물 위주로 골라 봤어요."), sampleWorks());
    }

    private static List<AiChatMockEvent> answer(String status, List<String> tokens, List<AiChatRecommendedWork> works) {
        List<AiChatMockEvent> events = new ArrayList<>();
        events.add(new AiChatMockEvent("progress", new AiChatProgressEvent("SEARCHING", "작품 찾는 중…"), 0L));
        if (!works.isEmpty()) {
            events.add(new AiChatMockEvent("progress", new AiChatProgressEvent("FOUND", "후보 12개 찾음"), STEP_DELAY_MILLIS));
        }
        tokens.forEach(text -> events.add(new AiChatMockEvent("token", new AiChatTokenEvent(text), TOKEN_DELAY_MILLIS)));
        events.add(new AiChatMockEvent("works", new AiChatWorksEvent(works, PIPELINE_VERSION), TOKEN_DELAY_MILLIS));
        events.add(new AiChatMockEvent("done", new AiChatDoneEvent(MOCK_MESSAGE_ID, status), TOKEN_DELAY_MILLIS));
        return events;
    }

    private static List<AiChatRecommendedWork> sampleWorks() {
        return List.of(
                new AiChatRecommendedWork(1, 1L, "목업 작품 1", null, "카카오페이지", "로판",
                        "은발 유아 주인공과 귀족 보호자가 나오는 육아물이에요.",
                        List.of(new AiChatMatchedClue("은발 보라 눈 유아", "COVER"), new AiChatMatchedClue("육아물", "TAG"))),
                new AiChatRecommendedWork(2, 2L, "목업 작품 2", null, "네이버 웹툰", "드라마",
                        "입양된 막내가 가족의 사랑을 받는 이야기예요.",
                        List.of(new AiChatMatchedClue("입양 · 가족물", "DESCRIPTION"))),
                new AiChatRecommendedWork(3, 3L, "목업 작품 3", null, "리디", "로판",
                        "냉정한 공작이 아이를 맡게 되는 로판이에요.",
                        List.of(new AiChatMatchedClue("공작 보호자", "DESCRIPTION"))));
    }
}
