package com.storix.domain.domains.search.dto;

import com.storix.domain.domains.search.helper.HangulTextHelper;
import com.storix.domain.domains.works.domain.Works;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record WorksDocument(
        Long worksId,
        String worksName,
        String worksNameChosung,
        String worksNameJamo,
        List<String> authors,
        String worksType,
        String genre
) {

    public static WorksDocument from(Works works) {
        List<String> authors = Stream.of(works.getAuthor(), works.getIllustrator(), works.getOriginalAuthor())
                .filter(Objects::nonNull)
                .map(HangulTextHelper::normalize)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();

        return new WorksDocument(
                works.getId(),
                HangulTextHelper.normalize(works.getWorksName()),
                HangulTextHelper.chosung(works.getWorksName()),
                HangulTextHelper.jamo(works.getWorksName()),
                authors,
                works.getWorksType() != null ? works.getWorksType().name() : null,
                works.getGenre() != null ? works.getGenre().name() : null
        );
    }
}
