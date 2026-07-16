package com.storix.api.domain.bannedword.usecase;

import com.storix.api.domain.bannedword.controller.dto.BannedWordBulkCreateRequest;
import com.storix.api.domain.bannedword.controller.dto.BannedWordCreateRequest;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.bannedword.dto.BannedWordPageResponse;
import com.storix.domain.domains.bannedword.exception.BannedWordCsvParseException;
import com.storix.domain.domains.bannedword.service.BannedWordAdminService;
import com.storix.domain.domains.bannedword.service.BannedWordMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminBannedWordUseCase {

    private final BannedWordAdminService bannedWordAdminService;
    private final BannedWordMatcher bannedWordMatcher;

    public BannedWordPageResponse searchBannedWords(String keyword, Pageable pageable) {
        return BannedWordPageResponse.from(bannedWordAdminService.search(keyword, pageable));
    }

    public void addBannedWord(BannedWordCreateRequest request) {
        bannedWordAdminService.addWord(request.word());   // 트랜잭션
        bannedWordMatcher.reload();                        // 비 트랜잭션 (DB 커밋 후 캐시 갱신)
    }

    public void addBannedWords(BannedWordBulkCreateRequest request) {
        bannedWordAdminService.addWords(request.words());
        bannedWordMatcher.reload();
    }

    public void addBannedWordsFromCsv(MultipartFile file) {
        bannedWordAdminService.addWords(parseWords(file));
        bannedWordMatcher.reload();
    }

    public void deleteBannedWord(Long bannedWordId) {
        bannedWordAdminService.deleteWord(bannedWordId);
        bannedWordMatcher.reload();
    }

    public void reloadCache() {
        bannedWordMatcher.reload();
    }

    private List<String> parseWords(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1) // 첫 줄은 헤더(slang)이므로 스킵
                    .map(AdminBannedWordUseCase::extractWord)
                    .filter(word -> !word.isBlank())
                    .distinct()
                    .toList();
        } catch (IOException e) {
            throw BannedWordCsvParseException.EXCEPTION;
        }
    }

    private static String extractWord(String line) {
        String firstColumn = line.split(",", -1)[0].trim();
        if (firstColumn.length() >= 2 && firstColumn.startsWith("\"") && firstColumn.endsWith("\"")) {
            firstColumn = firstColumn.substring(1, firstColumn.length() - 1);
        }
        return firstColumn.trim();
    }
}
