package com.storix.domain.domains.search.adaptor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.storix.domain.domains.search.config.FeedIndexProperties;
import com.storix.domain.domains.search.dto.FeedSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedSearchAdaptor {

    // 작품명 · 별칭이 맞으면 그 작품 이야기일 가능성이 커서 본문보다 무겁게 본다
    private static final String WORKS_NAME_FIELD = "worksName^2";
    private static final String NICKNAMES_FIELD = "nicknames^2";

    private final ElasticsearchClient client;
    private final FeedIndexProperties feedIndexProperties;

    // 비어 있으면 ES 가 응답하지 않은 것이니 MySQL 검색으로 돌린다
    public Optional<FeedSearchResult> searchBoardIds(String keyword, List<Long> excludedUserIds, int page, int size) {
        try {
            // nori 는 문맥에 따라 같은 단어를 다르게 쪼개 놓칠 수 있어 두 글자 조각이 모두 들어 있으면 찾고, 순위는 nori 점수로
            BoolQuery.Builder bool = new BoolQuery.Builder()
                    .should(q -> q.multiMatch(m -> m.query(keyword).fields("content", WORKS_NAME_FIELD, NICKNAMES_FIELD).type(TextQueryType.CrossFields).operator(Operator.And)))
                    .should(q -> q.multiMatch(m -> m.query(keyword).fields("content.bigram", "worksName.bigram", "nicknames.bigram").type(TextQueryType.CrossFields).operator(Operator.And)))
                    .minimumShouldMatch("1");
            if (!excludedUserIds.isEmpty()) {
                List<FieldValue> values = excludedUserIds.stream().map(FieldValue::of).toList();
                bool.mustNot(q -> q.terms(t -> t.field("userId").terms(v -> v.value(values))));
            }

            SearchResponse<Void> response = client.search(s -> s
                    .index(feedIndexProperties.alias())
                    .query(bool.build()._toQuery())
                    .sort(so -> so.score(sc -> sc.order(SortOrder.Desc)))
                    .sort(so -> so.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                    .from(page * size)
                    .size(size + 1)
                    .trackTotalHits(t -> t.enabled(false))
                    .source(src -> src.fetch(false)), Void.class);

            List<Long> ids = response.hits().hits().stream()
                    .map(Hit::id)
                    .map(Long::valueOf)
                    .toList();
            boolean hasNext = ids.size() > size;
            return Optional.of(new FeedSearchResult(hasNext ? ids.subList(0, size) : ids, hasNext));
        } catch (Exception e) {
            log.warn(">>> [FeedSearch] ES 검색 실패 keyword={}, cause={}", keyword, e.getMessage());
            return Optional.empty();
        }
    }
}
