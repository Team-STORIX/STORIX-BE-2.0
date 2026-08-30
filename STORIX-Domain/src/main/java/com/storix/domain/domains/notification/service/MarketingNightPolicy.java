package com.storix.domain.domains.notification.service;

import com.storix.common.utils.NightWindow;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MarketingNightPolicy {

    private static final Profiles IGNORED_PROFILES = Profiles.of("local", "dev");

    private final Environment environment;

    public boolean isBlocked(LocalDateTime sendAt) {
        // local, dev는 시간대 상관없이 발송 테스트가 가능해야 한다
        if (environment.acceptsProfiles(IGNORED_PROFILES)) {
            return false;
        }
        return NightWindow.isNight(sendAt);
    }
}
