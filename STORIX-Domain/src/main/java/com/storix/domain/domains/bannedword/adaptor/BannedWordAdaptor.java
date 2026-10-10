package com.storix.domain.domains.bannedword.adaptor;

import com.storix.domain.domains.bannedword.domain.BannedWord;
import com.storix.domain.domains.bannedword.exception.BannedWordNotFoundException;
import com.storix.domain.domains.bannedword.repository.BannedWordRepository;
import com.storix.domain.domains.bannedword.service.AdminKeywordMatcher;
import com.storix.domain.domains.bannedword.service.BannedWordMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BannedWordAdaptor {

    private final BannedWordMatcher bannedWordMatcher;
    private final AdminKeywordMatcher adminKeywordMatcher;
    private final BannedWordRepository bannedWordRepository;

    // DB로 관리되는 금칙어 포함 여부
    public boolean containsBannedWord(String text) {
        return bannedWordMatcher.containsBannedWord(text);
    }

    // 관리자/운영 예약 키워드 포함 여부
    public boolean containsAdminKeyword(String text) {
        return adminKeywordMatcher.containsAdminKeyword(text);
    }

    public Page<BannedWord> search(String keyword, Pageable pageable) {
        return keyword == null || keyword.isBlank()
                ? bannedWordRepository.findAll(pageable)
                : bannedWordRepository.findByWordContaining(keyword, pageable);
    }

    public boolean existsByWord(String word) {
        return bannedWordRepository.existsByWord(word);
    }

    public Set<String> findAllWords() {
        return new HashSet<>(bannedWordRepository.findAllWords());
    }

    public void save(BannedWord bannedWord) {
        bannedWordRepository.save(bannedWord);
    }

    public void saveAll(List<BannedWord> bannedWords) {
        bannedWordRepository.saveAll(bannedWords);
    }

    public void deleteById(Long id) {
        if (!bannedWordRepository.existsById(id)) {
            throw BannedWordNotFoundException.EXCEPTION;
        }
        bannedWordRepository.deleteById(id);
    }
}
