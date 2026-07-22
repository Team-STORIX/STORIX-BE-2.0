package com.storix.domain.domains.bannedword.service;

import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Component;

import java.util.List;

// 관리자/운영 주체를 사칭할 수 있는 예약 키워드를 검사
@Component
public class AdminKeywordMatcher {

    // 사칭 방지용 예약 키워드
    private static final List<String> ADMIN_KEYWORDS = List.of(
            "관리자", "운영자", "운영팀", "매니저", "고객센터", "고객지원", "공식",
            "스토릭스", "storix", "admin", "administrator", "manager", "system", "시스템"
    );

    private final Trie trie;

    public AdminKeywordMatcher() {
        Trie.TrieBuilder builder = Trie.builder().ignoreCase();
        ADMIN_KEYWORDS.forEach(builder::addKeyword);
        this.trie = builder.build();
    }

    public boolean containsAdminKeyword(String text) {
        return text != null && !text.isBlank() && trie.containsMatch(text);
    }
}
