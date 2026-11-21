package com.storix.domain.domains.aichat.service;

import com.storix.domain.domains.aichat.adaptor.AiChatAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatMessageAdaptor;
import com.storix.domain.domains.aichat.adaptor.AiChatSettingAdaptor;
import com.storix.domain.domains.aichat.domain.AiChat;
import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.domain.AiChatSetting;
import com.storix.domain.domains.aichat.dto.AiChatOnboardingLine;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import com.storix.domain.domains.works.domain.WorksType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiChatSettingService {

    private final AiChatSettingAdaptor aiChatSettingAdaptor;
    private final AiChatAdaptor aiChatAdaptor;
    private final AiChatMessageAdaptor aiChatMessageAdaptor;

    // 저장한 적 없으면 기본값. 조회만으로는 행을 만들지 않는다
    @Transactional(readOnly = true)
    public AiChatSettingInfo find(Long userId) {
        return aiChatSettingAdaptor.findByUserId(userId)
                .map(AiChatSettingInfo::from)
                .orElseGet(() -> AiChatSettingInfo.from(AiChatSetting.defaultOf(userId)));
    }

    // onboardingVersion 이 있고 아직 그 버전을 안 봤으면 온보딩 말풍선을 대화 기록에 남긴다
    @Transactional
    public AiChatSettingInfo save(Long userId, boolean adultEnabled, Set<WorksType> worksTypes,
                                  Integer onboardingVersion, List<AiChatOnboardingLine> onboardingLines) {
        AiChatSetting setting = aiChatSettingAdaptor.findByUserId(userId)
                .orElseGet(() -> AiChatSetting.defaultOf(userId));
        setting.update(adultEnabled, worksTypes);

        if (onboardingVersion != null && !setting.hasSeenOnboarding(onboardingVersion)) {
            setting.completeOnboarding(onboardingVersion);
            AiChat chat = aiChatAdaptor.findOrCreate(userId);
            onboardingLines.forEach(line -> aiChatMessageAdaptor.save(
                    AiChatMessage.onboarding(chat.getId(), line.role(), line.content())));
            chat.touch(LocalDateTime.now());
        }
        return AiChatSettingInfo.from(aiChatSettingAdaptor.save(setting));
    }
}
