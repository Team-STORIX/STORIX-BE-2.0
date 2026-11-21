package com.storix.domain.domains.aichat.adaptor;

import com.storix.domain.domains.aichat.domain.AiChatSetting;
import com.storix.domain.domains.aichat.exception.AiChatSettingNotFoundException;
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

    // 없으면 기본값으로 만들고 잠가서 돌려준다. 같은 사용자의 저장이 차례로 돈다. 트랜잭션 안에서 불러야 한다
    public AiChatSetting findForUpdateOrCreate(Long userId) {
        aiChatSettingRepository.insertIfAbsent(userId);
        return aiChatSettingRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> AiChatSettingNotFoundException.EXCEPTION);
    }

    public int deleteByUserId(Long userId) {
        return aiChatSettingRepository.deleteByUserId(userId);
    }
}
