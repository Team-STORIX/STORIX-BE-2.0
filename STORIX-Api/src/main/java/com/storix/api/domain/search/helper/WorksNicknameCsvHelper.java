package com.storix.api.domain.search.helper;

import com.storix.domain.domains.search.dto.WorksNicknameEntry;
import com.storix.domain.domains.search.exception.WorksNicknameCsvParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorksNicknameCsvHelper {

    private static final String HEADER_PREFIX = "works_id";
    private static final String BOM = "\uFEFF";

    public List<WorksNicknameEntry> parse(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<WorksNicknameEntry> entries = new ArrayList<>();
            boolean firstLine = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    line = line.startsWith(BOM) ? line.substring(1) : line;
                    if (line.trim().toLowerCase().startsWith(HEADER_PREFIX)) continue;
                }
                if (line.isBlank()) continue;
                entries.add(toEntry(splitColumns(line)));
            }
            return entries;
        } catch (IOException e) {
            throw WorksNicknameCsvParseException.EXCEPTION;
        }
    }

    private WorksNicknameEntry toEntry(List<String> columns) {
        String nickname = columns.size() > 1 ? columns.get(1) : null;
        try {
            return new WorksNicknameEntry(Long.valueOf(columns.get(0).trim()), nickname);
        } catch (NumberFormatException e) {
            return new WorksNicknameEntry(null, nickname);
        }
    }

    // 작품명에 쉼표가 들어갈 수 있어 따옴표로 감싼 칸을 한 칸으로 읽는다
    private List<String> splitColumns(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        columns.add(current.toString());
        return columns;
    }
}
