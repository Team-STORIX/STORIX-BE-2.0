package com.storix.domain.domains.bannedword.adaptor;

import com.storix.domain.domains.bannedword.service.BannedWordMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 다른 도메인이 금칙어 검사가 필요할 때 거치는 어댑터. BannedWordMatcher(캐시/아호코라식 매칭)를 직접 주입받지 않도록 한다.
@Component
@RequiredArgsConstructor
public class BannedWordAdaptor {

    private final BannedWordMatcher bannedWordMatcher;

    public boolean containsBannedWord(String text) {
        return bannedWordMatcher.containsBannedWord(text);
    }
}
