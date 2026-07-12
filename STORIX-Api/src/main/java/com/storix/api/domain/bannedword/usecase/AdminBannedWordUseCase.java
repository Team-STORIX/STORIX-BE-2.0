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

    private static final String CSV_HEADER = "slang";

    private final BannedWordAdminService bannedWordAdminService;
    private final BannedWordMatcher bannedWordMatcher;

    public BannedWordPageResponse searchBannedWords(String keyword, Pageable pageable) {
        return BannedWordPageResponse.from(bannedWordAdminService.search(keyword, pageable));
    }

    // 캐시 갱신은 BannedWordCacheReloadListener가 변경 트랜잭션 커밋 후 수행함
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
        bannedWordMatcher.reload();
    }

    private List<String> parseWords(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().toList();

            int start = !lines.isEmpty() && CSV_HEADER.equalsIgnoreCase(extractWord(lines.get(0))) ? 1 : 0;

            return lines.stream()
                    .skip(start)
                    .map(AdminBannedWordUseCase::extractWord)
                    .filter(word -> !word.isBlank())
                    .distinct()
                    .toList();
        } catch (IOException e) {
            throw BannedWordCsvParseException.EXCEPTION;
        }
    }

    private static String extractWord(String line) {
        String trimmed = stripBom(line).trim();
        if (!trimmed.startsWith("\"")) {
            return trimmed.split(",", 2)[0].trim();
        }

        StringBuilder firstColumn = new StringBuilder();
        for (int i = 1; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '"') {
                if (i + 1 < trimmed.length() && trimmed.charAt(i + 1) == '"') {
                    firstColumn.append('"');
                    i++;
                    continue;
                }
                break;
            }
            firstColumn.append(c);
        }
        return firstColumn.toString().trim();
    }

    private static String stripBom(String line) {
        return line.startsWith("\uFEFF") ? line.substring(1) : line;
    }
}
