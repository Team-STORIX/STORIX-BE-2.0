package com.storix.domain.domains.bannedword.service;

import com.storix.domain.domains.bannedword.domain.BannedWord;
import com.storix.domain.domains.bannedword.exception.BannedWordNotFoundException;
import com.storix.domain.domains.bannedword.exception.DuplicateBannedWordException;
import com.storix.domain.domains.bannedword.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BannedWordAdminService {

    private final BannedWordRepository bannedWordRepository;
    private final BannedWordMatcher bannedWordMatcher;

    @Transactional(readOnly = true)
    public Page<BannedWord> search(String keyword, Pageable pageable) {
        return keyword == null || keyword.isBlank()
                ? bannedWordRepository.findAll(pageable)
                : bannedWordRepository.findByWordContaining(keyword, pageable);
    }

    public void addWord(String word) {
        if (bannedWordRepository.existsByWord(word)) {
            throw DuplicateBannedWordException.EXCEPTION;
        }
        bannedWordRepository.save(BannedWord.builder().word(word).build());
        bannedWordMatcher.reload();
    }

    public void addWords(List<String> words) {
        List<BannedWord> newWords = words.stream()
                .distinct()
                .filter(word -> !bannedWordRepository.existsByWord(word))
                .map(word -> BannedWord.builder().word(word).build())
                .toList();

        bannedWordRepository.saveAll(newWords);
        bannedWordMatcher.reload();
    }

    public void deleteWord(Long id) {
        if (!bannedWordRepository.existsById(id)) {
            throw BannedWordNotFoundException.EXCEPTION;
        }
        bannedWordRepository.deleteById(id);
        bannedWordMatcher.reload();
    }

    public void reloadCache() {
        bannedWordMatcher.reload();
    }
}
