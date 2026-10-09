package com.storix.domain.domains.search.adaptor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.storix.domain.domains.search.helper.HangulTextHelper;
import com.storix.domain.domains.search.config.WorksIndexProperties;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.WorksType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorksSearchAdaptor {

    private static final int MAX_IDS = 1000;
    private static final int MAX_FUZZY_IDS = 100;
    private static final String FUZZY_MINIMUM_MATCH = "70%";
    private static final int MIN_FUZZY_LENGTH = 4;

    private final ElasticsearchClient client;
    private final WorksIndexProperties worksIndexProperties;

    // 비어 있으면 MySQL 검색으로 돌린다. ES 가 응답하지 않거나, 검색어가 특수문자뿐이라 ES 로 찾을 글자가 없을 때다
    public Optional<List<Long>> searchIds(String keyword, List<WorksType> worksTypes, List<Genre> genres) {
        String normalized = HangulTextHelper.normalize(keyword);
        if (normalized.isEmpty()) return Optional.empty();

        try {
            boolean chosungOnly = HangulTextHelper.isChosungOnly(normalized);
            List<Long> ids = search(strictQuery(normalized, chosungOnly, worksTypes, genres), MAX_IDS);

            // 오타 교정은 결과가 없을 때만 한다. 정렬이 관련도순이 아니라 비슷한 작품이 섞이면 앞에 나오고, 짧은 검색어는 엉뚱한 작품이 걸린다
            if (ids.isEmpty() && !chosungOnly && normalized.length() >= MIN_FUZZY_LENGTH) {
                ids = search(fuzzyQuery(normalized, worksTypes, genres), MAX_FUZZY_IDS);
            }
            return Optional.of(ids);
        } catch (Exception e) {
            log.warn(">>> [WorksSearch] ES 검색 실패 keyword={}, cause={}", keyword, e.getMessage());
            return Optional.empty();
        }
    }

    // 적재 중복 의심 후보 조회
    public Optional<List<Long>> findSimilarIds(String titleKey, WorksType worksType, int size) {
        if (titleKey == null || titleKey.isEmpty()) return Optional.of(List.of());

        BoolQuery.Builder bool = new BoolQuery.Builder()
                .should(q -> q.wildcard(w -> w.field("worksName").value("*" + titleKey + "*")))
                .should(q -> q.match(m -> m
                        .field("worksNameJamo")
                        .query(HangulTextHelper.jamo(titleKey))
                        .minimumShouldMatch(FUZZY_MINIMUM_MATCH)))
                .minimumShouldMatch("1");
        addFilters(bool, List.of(worksType), null);

        try {
            return Optional.of(search(bool.build()._toQuery(), size));
        } catch (Exception e) {
            log.warn(">>> [WorksSearch] 중복 후보 조회 실패 titleKey={}, cause={}", titleKey, e.getMessage());
            return Optional.empty();
        }
    }

    private List<Long> search(Query query, int size) throws IOException {
        SearchResponse<Void> response = client.search(s -> s
                .index(worksIndexProperties.alias())
                .query(query)
                .size(size)
                .source(src -> src.fetch(false)), Void.class);

        return response.hits().hits().stream()
                .map(Hit::id)
                .map(Long::valueOf)
                .toList();
    }

    private Query strictQuery(String normalized, boolean chosungOnly, List<WorksType> worksTypes, List<Genre> genres) {
        String pattern = "*" + normalized + "*";

        BoolQuery.Builder bool = new BoolQuery.Builder()
                .should(q -> q.wildcard(w -> w.field("worksName").value(pattern)))
                .should(q -> q.wildcard(w -> w.field("authors").value(pattern)))
                .should(q -> q.wildcard(w -> w.field("nicknames").value(pattern)))
                .minimumShouldMatch("1");
        if (chosungOnly) {
            bool.should(q -> q.wildcard(w -> w.field("worksNameChosung").value(pattern)));
        }
        addFilters(bool, worksTypes, genres);
        return bool.build()._toQuery();
    }

    private Query fuzzyQuery(String normalized, List<WorksType> worksTypes, List<Genre> genres) {
        BoolQuery.Builder bool = new BoolQuery.Builder()
                .must(q -> q.match(m -> m
                        .field("worksNameJamo")
                        .query(HangulTextHelper.jamo(normalized))
                        .minimumShouldMatch(FUZZY_MINIMUM_MATCH)));
        addFilters(bool, worksTypes, genres);
        return bool.build()._toQuery();
    }

    private void addFilters(BoolQuery.Builder bool, List<WorksType> worksTypes, List<Genre> genres) {
        if (worksTypes != null && !worksTypes.isEmpty()) {
            List<FieldValue> values = worksTypes.stream().map(type -> FieldValue.of(type.name())).toList();
            bool.filter(q -> q.terms(t -> t.field("worksType").terms(v -> v.value(values))));
        }
        if (genres != null && !genres.isEmpty()) {
            List<FieldValue> values = genres.stream().map(genre -> FieldValue.of(genre.name())).toList();
            bool.filter(q -> q.terms(t -> t.field("genre").terms(v -> v.value(values))));
        }
    }
}
