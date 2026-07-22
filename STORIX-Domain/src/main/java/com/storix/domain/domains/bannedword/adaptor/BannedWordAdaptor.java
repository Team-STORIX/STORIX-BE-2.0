package com.storix.domain.domains.bannedword.adaptor;

import com.storix.domain.domains.bannedword.service.AdminKeywordMatcher;
import com.storix.domain.domains.bannedword.service.BannedWordMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BannedWordAdaptor {

    private final BannedWordMatcher bannedWordMatcher;
    private final AdminKeywordMatcher adminKeywordMatcher;

    // DB로 관리되는 금칙어 포함 여부
    public boolean containsBannedWord(String text) {
        return bannedWordMatcher.containsBannedWord(text);
    }

    // 관리자/운영 예약 키워드 포함 여부
    public boolean containsAdminKeyword(String text) {
        return adminKeywordMatcher.containsAdminKeyword(text);
    }
}
