package com.storix.domain.domains.bannedword.service;

import com.storix.domain.domains.bannedword.domain.BannedWord;
import com.storix.domain.domains.bannedword.event.BannedWordChangedEvent;
import com.storix.domain.domains.bannedword.exception.BannedWordNotFoundException;
import com.storix.domain.domains.bannedword.exception.DuplicateBannedWordException;
import com.storix.domain.domains.bannedword.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BannedWordAdminService {

    private final BannedWordRepository bannedWordRepository;
    private final ApplicationEventPublisher eventPublisher;

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
            throw DuplicateBannedWordException.EXCEPTION;
        }
        eventPublisher.publishEvent(new BannedWordChangedEvent());
    }

    public void addWords(List<String> words) {
        Set<String> seenWords = bannedWordRepository.findAllWords().stream()
                .map(word -> word.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));

        List<BannedWord> newWords = words.stream()
                .map(String::trim)
                .filter(word -> !word.isBlank())
                .filter(word -> seenWords.add(word.toLowerCase(Locale.ROOT)))
                .map(word -> BannedWord.builder().word(word).build())
                .toList();

        if (newWords.isEmpty()) {
            return;
        }

        try {
            bannedWordRepository.saveAll(newWords);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateBannedWordException.EXCEPTION;
        }
        eventPublisher.publishEvent(new BannedWordChangedEvent());
    }

    public void deleteWord(Long id) {
        if (!bannedWordRepository.existsById(id)) {
            throw BannedWordNotFoundException.EXCEPTION;
        }
        bannedWordRepository.deleteById(id);
        eventPublisher.publishEvent(new BannedWordChangedEvent());
    }
}
