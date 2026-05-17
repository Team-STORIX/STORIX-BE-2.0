package com.storix.domain.domains.pushdevice.service;

import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FCM 발송 관련 서비스
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushDispatchService {

    private final PushDeviceAdaptor pushDeviceAdaptor;

    // FCM invalid 토큰 일괄 비활성화
    @Transactional
    public void deactivateTokens(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return;
        int affected = pushDeviceAdaptor.deactivateByFcmTokens(tokens);
        log.info(">>>> [PushDispatch] deactivate invalid tokens count={}, affected={}", tokens.size(), affected);
    }

    // 발송 성공한 토큰들의 lastSuccessAt 갱신
    @Transactional
    public void markTokensSuccess(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return;
        pushDeviceAdaptor.markFcmTokensSuccess(tokens, LocalDateTime.now());
    }
}
