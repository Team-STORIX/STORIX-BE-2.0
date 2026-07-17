package com.storix.infrastructure.external.notification.fcm;

import com.storix.domain.domains.pushdevice.service.PushDispatchService;
import com.storix.infrastructure.external.notification.dto.MulticastResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushExecutor {

    private final FcmSender fcmSender;
    private final PushDispatchService pushDispatchService;

    public MulticastResult sendAndApply(List<String> tokens, Map<String, String> data) {
        MulticastResult result = fcmSender.sendMulticast(tokens, data);
        if (!result.invalidTokens().isEmpty()) {
            pushDispatchService.deactivateTokens(result.invalidTokens());
        }
        if (!result.successTokens().isEmpty()) {
            pushDispatchService.markTokensSuccess(result.successTokens());
        }
        return result;
    }
}
