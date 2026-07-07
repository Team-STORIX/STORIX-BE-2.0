package com.storix.domain.domains.bannedword.service;

import com.storix.domain.domains.bannedword.domain.BannedWord;
import com.storix.domain.domains.bannedword.exception.BannedWordNotFoundException;
import com.storix.domain.domains.bannedword.exception.DuplicateBannedWordException;
import com.storix.domain.domains.bannedword.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BannedWordAdminService {

    private final BannedWordRepository bannedWordRepository;

    @Transactional(readOnly = true)
    public Page<BannedWord> search(String keyword, Pageable pageable) {
        return keyword == null || keyword.isBlank()
                ? bannedWordRepository.findAll(pageable)
                : bannedWordRepository.findByWordContaining(keyword, pageable);
    }

    public void addWord(String word) {
        String normalized = word.trim();
        if (bannedWordRepository.existsByWord(normalized)) {
            throw DuplicateBannedWordException.EXCEPTION;
        }
        try {
            bannedWordRepository.save(BannedWord.builder().word(normalized).build());
        } catch (DataIntegrityViolationException e) {
            // uk_banned_word 제약 위반(동시 요청 등)은 도메인 예외로 변환
            throw DuplicateBannedWordException.EXCEPTION;
        }
    }

    public void addWords(List<String> words) {
        // 단어마다 existsByWord 쿼리가 나가지 않도록 기존 단어를 한 번에 조회해 메모리에서 비교
        Set<String> existingWords = new HashSet<>(bannedWordRepository.findAllWords());

        List<BannedWord> newWords = words.stream()
                .map(String::trim)
                .filter(word -> !word.isBlank())
                .distinct()
                .filter(word -> !existingWords.contains(word))
                .map(word -> BannedWord.builder().word(word).build())
                .toList();

        bannedWordRepository.saveAll(newWords);
    }

    public void deleteWord(Long id) {
        if (!bannedWordRepository.existsById(id)) {
            throw BannedWordNotFoundException.EXCEPTION;
        }
        bannedWordRepository.deleteById(id);
    }
}
