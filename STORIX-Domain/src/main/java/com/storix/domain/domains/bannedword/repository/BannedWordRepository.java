package com.storix.domain.domains.bannedword.repository;

import com.storix.domain.domains.bannedword.domain.BannedWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BannedWordRepository extends JpaRepository<BannedWord, Long> {

    @Query("select b.word from BannedWord b")
    List<String> findAllWords();

    boolean existsByWord(String word);

    Page<BannedWord> findByWordContaining(String keyword, Pageable pageable);
}
