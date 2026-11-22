package com.storix.api.domain.aichat.helper;

import com.storix.api.domain.aichat.controller.dto.AiChatOnboardingOption;
import com.storix.api.domain.aichat.controller.dto.AiChatOnboardingStep;
import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.dto.AiChatOnboardingLine;
import com.storix.domain.domains.aichat.exception.AiChatInvalidOnboardingVersionException;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AiChatOnboardingHelper {

    // 멘트를 크게 바꾸면 올림. 본 버전이 낮은 사용자에게 온보딩 다시 노출
    public static final int VERSION = 1;

    private static final String CHATBOT_NAME = "○○";

    public static final List<String> EXAMPLE_QUESTIONS = List.of(
            "회귀 복수물 판타지 웹소설 추천해 줘",
            "계약결혼 로판 중에 완결된 거 있어?",
            "집착남 나오는 웹툰 찾아 줘"
    );

    private static final Map<WorksType, String> WORKS_TYPE_LABELS = Map.of(
            WorksType.WEBTOON, "웹툰",
            WorksType.WEBNOVEL, "웹소설",
            WorksType.BOOK, "단행본"
    );

    private AiChatOnboardingHelper() {
    }

    public static void requireCurrentVersion(int version) {
        if (version != VERSION) {
            throw AiChatInvalidOnboardingVersionException.EXCEPTION;
        }
    }

    public static List<AiChatOnboardingLine> transcript(boolean adultVerified, boolean adultEnabled,
                                                        Collection<WorksType> worksTypes) {
        List<AiChatOnboardingLine> lines = new ArrayList<>();
        for (AiChatOnboardingStep step : steps(adultVerified)) {
            lines.add(new AiChatOnboardingLine(AiChatMessageRole.CHATBOT, step.message()));
            if (step.step().equals("ADULT")) {
                lines.add(new AiChatOnboardingLine(AiChatMessageRole.USER, adultEnabled ? "같이 추천받기" : "빼고 추천받기"));
            }
            if (step.step().equals("WORKS_TYPE")) {
                String answer = worksTypes.isEmpty() ? "상관없음" : worksTypes.stream()
                        .sorted()
                        .map(WORKS_TYPE_LABELS::get)
                        .collect(Collectors.joining(", "));
                lines.add(new AiChatOnboardingLine(AiChatMessageRole.USER, answer));
            }
        }
        return lines;
    }

    public static List<AiChatOnboardingStep> steps(boolean adultVerified) {
        List<AiChatOnboardingStep> steps = new ArrayList<>();
        steps.add(AiChatOnboardingStep.message("GREETING",
                "안녕하세요! 저는 스토릭스 작품 탐색 도우미 " + CHATBOT_NAME + "예요. "
                        + "찾고 싶은 작품을 편하게 말해 주시면 딱 맞는 작품을 골라 드릴게요."));
        if (adultVerified) {
            steps.add(AiChatOnboardingStep.singleSelect("ADULT",
                    "19세 이상 작품도 같이 추천해 드릴까요?",
                    List.of(new AiChatOnboardingOption("true", "같이 추천받기"),
                            new AiChatOnboardingOption("false", "빼고 추천받기"))));
        }
        steps.add(AiChatOnboardingStep.multiSelect("WORKS_TYPE",
                "주로 어떤 작품을 추천받고 싶으세요?",
                List.of(new AiChatOnboardingOption("WEBTOON", "웹툰"),
                        new AiChatOnboardingOption("WEBNOVEL", "웹소설"),
                        new AiChatOnboardingOption("BOOK", "단행본"),
                        new AiChatOnboardingOption("ANY", "상관없음"))));
        steps.add(AiChatOnboardingStep.message("DONE",
                "좋아요, 고른 내용은 챗봇 설정에 저장해 뒀어요. "
                        + "바꾸고 싶으면 언제든 설정에서 바꿀 수 있어요. 그럼 어떤 작품을 찾아 볼까요?"));
        return steps;
    }
}
