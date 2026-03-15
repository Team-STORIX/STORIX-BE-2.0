package com.storix.domain.domains.works.application.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArtistNameParseHelper {

    @Transactional
    public String buildArtistName(String originalAuthor, String author, String illustrator) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();

        addTokens(tokens, originalAuthor);
        addTokens(tokens, author);
        addTokens(tokens, illustrator);

        return String.join(",", tokens);
    }

    @Transactional
    public void addTokens(Set<String> out, String value) {
        if (value == null) return;

        String trimmed = value.trim();
        if (trimmed.isEmpty()) return;

        for (String token : trimmed.split(",")) {
            String t = token.trim();
            if (!t.isEmpty()) {
                t = t.replaceAll("\\s+", " ");
                out.add(t);
            }
        }
    }

}
