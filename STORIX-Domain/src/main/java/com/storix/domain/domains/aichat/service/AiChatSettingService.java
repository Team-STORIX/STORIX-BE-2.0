package com.storix.domain.domains.aichat.service;

import com.storix.domain.domains.aichat.adaptor.AiChatSettingAdaptor;
import com.storix.domain.domains.aichat.domain.AiChatSetting;
import com.storix.domain.domains.aichat.dto.AiChatSettingInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiChatSettingService {

    private final AiChatSettingAdaptor aiChatSettingAdaptor;

    // 저장한 적 없으면 기본값. 조회만으로는 행을 만들지 않는다
    @Transactional(readOnly = true)
    public AiChatSettingInfo find(Long userId) {
        return aiChatSettingAdaptor.findByUserId(userId)
                .map(AiChatSettingInfo::from)
                .orElseGet(() -> AiChatSettingInfo.from(AiChatSetting.defaultOf(userId)));
    }
}
