package com.storix.api.domain.bannedword.helper;

import com.storix.domain.domains.bannedword.exception.BannedWordCsvParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class BannedWordCsvHelper {

    public List<String> parseWords(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1) // 첫 줄은 헤더(slang)이므로 스킵
                    .map(BannedWordCsvHelper::extractWord)
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
