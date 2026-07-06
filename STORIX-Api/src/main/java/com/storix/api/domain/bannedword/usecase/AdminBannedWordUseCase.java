package com.storix.api.domain.bannedword.usecase;

import com.storix.api.domain.bannedword.controller.dto.BannedWordBulkCreateRequest;
import com.storix.api.domain.bannedword.controller.dto.BannedWordCreateRequest;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.bannedword.dto.BannedWordPageResponse;
import com.storix.domain.domains.bannedword.exception.BannedWordCsvParseException;
import com.storix.domain.domains.bannedword.service.BannedWordAdminService;
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

    public BannedWordPageResponse searchBannedWords(String keyword, Pageable pageable) {
        return BannedWordPageResponse.from(bannedWordAdminService.search(keyword, pageable));
    }

    public void addBannedWord(BannedWordCreateRequest request) {
        bannedWordAdminService.addWord(request.word());
    }

    public void addBannedWords(BannedWordBulkCreateRequest request) {
        bannedWordAdminService.addWords(request.words());
    }

    public void addBannedWordsFromCsv(MultipartFile file) {
        bannedWordAdminService.addWords(parseWords(file));
    }

    public void deleteBannedWord(Long bannedWordId) {
        bannedWordAdminService.deleteWord(bannedWordId);
    }

    public void reloadCache() {
        bannedWordAdminService.reloadCache();
    }

    private List<String> parseWords(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(AdminBannedWordUseCase::extractWord)
                    .filter(word -> !word.isBlank() && !word.equalsIgnoreCase("slang"))
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
