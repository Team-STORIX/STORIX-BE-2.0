package com.storix.domain.domains.aichat.adaptor;

import com.storix.domain.domains.aichat.domain.AiChatSetting;
import com.storix.domain.domains.aichat.repository.AiChatSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AiChatSettingAdaptor {

    private final AiChatSettingRepository aiChatSettingRepository;

    public AiChatSetting save(AiChatSetting setting) {
        return aiChatSettingRepository.save(setting);
    }

    public Optional<AiChatSetting> findByUserId(Long userId) {
        return aiChatSettingRepository.findByUserId(userId);
    }

    public int deleteByUserId(Long userId) {
        return aiChatSettingRepository.deleteByUserId(userId);
    }
}
