package com.storix.domain.domains.bannedword.service;

import com.storix.domain.domains.bannedword.repository.BannedWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Component;

import java.util.List;

// DB에 적재된 금칙어를 앱 기동 시 메모리에 캐싱 및 아호코라식 알고리즘으로 텍스트 내 포함 여부를 검사
// 관리자가 금칙어를 추가/삭제하면 reload()를 호출해 캐시 갱신
@Component
@RequiredArgsConstructor
@Slf4j
public class BannedWordMatcher {

    private final BannedWordRepository bannedWordRepository;

    private volatile Trie trie = Trie.builder().ignoreCase().build();

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        List<String> words = bannedWordRepository.findAllWords();

        Trie.TrieBuilder builder = Trie.builder().ignoreCase();
        words.forEach(builder::addKeyword);
        this.trie = builder.build();

        log.info("[BannedWordMatcher] 금칙어 캐시를 갱신했습니다. (word={}개)", words.size());
    }

    public boolean containsBannedWord(String text) {
        return text != null && !text.isBlank() && trie.containsMatch(text);
    }
}
