package com.storix.api.domain.aichat.onboarding;

import com.storix.api.domain.aichat.controller.dto.AiChatOnboardingOption;
import com.storix.api.domain.aichat.controller.dto.AiChatOnboardingStep;

import java.util.ArrayList;
import java.util.List;

// 온보딩 멘트 · 선택지 · 예시 질문. 서버에 두어 앱 업데이트 없이 바꾼다
// 멘트를 크게 바꾸면 VERSION 을 올린다. 본 버전이 낮은 사용자에게 온보딩을 다시 보여 준다
public final class AiChatOnboardingScript {

    public static final int VERSION = 1;

    // 챗봇 이름이 정해지면 바꾼다
    private static final String CHATBOT_NAME = "○○";

    public static final List<String> EXAMPLE_QUESTIONS = List.of(
            "회귀 복수물 판타지 웹소설 추천해 줘",
            "계약결혼 로판 중에 완결된 거 있어?",
            "집착남 나오는 웹툰 찾아 줘"
    );

    private AiChatOnboardingScript() {
    }

    // 성인 인증이 안 된 사용자는 19금 질문을 건너뛰고 "빼고" 로 고정한다
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
