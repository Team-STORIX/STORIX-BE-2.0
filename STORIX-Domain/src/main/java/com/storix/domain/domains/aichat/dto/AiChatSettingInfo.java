package com.storix.domain.domains.aichat.dto;

import com.storix.domain.domains.aichat.domain.AiChatSetting;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.Set;

public record AiChatSettingInfo(
        boolean adultEnabled,
        Set<WorksType> worksTypes,
        Integer onboardingVersion
) {
    public static AiChatSettingInfo from(AiChatSetting setting) {
        return new AiChatSettingInfo(setting.isAdultEnabled(), Set.copyOf(setting.getWorksTypes()),
                setting.getOnboardingVersion());
    }

    public boolean hasSeenOnboarding(int currentVersion) {
        return onboardingVersion != null && onboardingVersion >= currentVersion;
    }
}
