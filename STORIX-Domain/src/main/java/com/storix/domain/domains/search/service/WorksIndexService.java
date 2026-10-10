package com.storix.domain.domains.search.service;

import static com.storix.common.utils.RedisKeyStatic.Search.WORKS_REINDEX_LOCK;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.storix.domain.domains.search.config.WorksIndexProperties;
import com.storix.domain.domains.search.dto.SearchReindexResponse;
import com.storix.domain.domains.search.dto.WorksDocument;
import com.storix.domain.domains.search.exception.SearchReindexInProgressException;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Works;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorksIndexService {

    private static final String INDEX_DEFINITION = "elasticsearch/works-index.json";
    private static final int CHUNK_SIZE = 500;

    private final ElasticsearchClient client;
    private final SearchIndexManager searchIndexManager;
    private final WorksIndexProperties worksIndexProperties;
    private final WorksAdaptor worksAdaptor;

    public boolean isAliasMissing() throws IOException {
        return searchIndexManager.isAliasMissing(worksIndexProperties.alias());
    }

    // 재색인 도중 바뀐 별칭은 새 인덱스에 빠질 수 있어 그동안은 별칭을 못 바꾸게 한다
    public void checkNotReindexing() {
        if (searchIndexManager.isReindexing(WORKS_REINDEX_LOCK)) throw SearchReindexInProgressException.EXCEPTION;
    }

    // 실패해도 DB 는 이미 바뀌었으니 다음 재색인 때 맞춰진다
    public void indexWorks(Long worksId) {
        try {
            Works works = worksAdaptor.findById(worksId);
            List<String> nicknames = worksAdaptor.loadNicknamesByWorksIds(List.of(worksId)).getOrDefault(worksId, List.of());
            WorksDocument document = WorksDocument.of(works, nicknames);
            // 응답 직후 어드민이 바로 검색해 볼 수 있게 반영될 때까지 기다린다
            client.index(i -> i.index(worksIndexProperties.alias()).id(String.valueOf(worksId)).document(document).refresh(Refresh.WaitFor).requireAlias(true));
        } catch (Exception e) {
            log.warn(">>> [WorksIndex] 작품 색인 실패 worksId={}, cause={}", worksId, e.getMessage());
        }
    }

    // 병합으로 없어진 작품 색인 삭제. 실패해도 다음 재색인 때 맞춰짐
    public void deleteWorks(List<Long> worksIds) {
        try {
            BulkRequest.Builder bulk = new BulkRequest.Builder().index(worksIndexProperties.alias()).refresh(Refresh.WaitFor);
            worksIds.forEach(worksId -> bulk.operations(op -> op.delete(d -> d.id(String.valueOf(worksId)))));
            BulkResponse response = client.bulk(bulk.build());
            if (response.errors()) log.warn(">>> [WorksIndex] 작품 일부 색인 삭제 실패 worksIds={}", worksIds);
        } catch (Exception e) {
            log.warn(">>> [WorksIndex] 작품 색인 삭제 실패 worksIds={}, cause={}", worksIds, e.getMessage());
        }
    }

    // 실패해도 다음 재색인 때 맞춰진다
    public void indexWorksBulk(List<Long> worksIds) {
        try {
            Map<Long, List<String>> nicknames = worksAdaptor.loadNicknamesByWorksIds(worksIds);
            List<WorksDocument> documents = worksAdaptor.findWorksByIds(worksIds).stream()
                    .map(works -> WorksDocument.of(works, nicknames.getOrDefault(works.getId(), List.of())))
                    .toList();
            searchIndexManager.bulkIndex(worksIndexProperties.alias(), documents, document -> String.valueOf(document.worksId()));
        } catch (Exception e) {
            log.warn(">>> [WorksIndex] 작품 색인 실패 count={}, cause={}", worksIds.size(), e.getMessage());
        }
    }

    public SearchReindexResponse reindexAll() {
        return searchIndexManager.reindex(worksIndexProperties.alias(), INDEX_DEFINITION, WORKS_REINDEX_LOCK, this::indexAllWorks);
    }

    private long indexAllWorks(String index) throws IOException {
        long count = 0;
        long lastWorksId = 0;

        while (true) {
            List<Works> chunk = worksAdaptor.findWorksChunkAfter(lastWorksId, CHUNK_SIZE);
            if (chunk.isEmpty()) return count;

            Map<Long, List<String>> nicknames = worksAdaptor.loadNicknamesByWorksIds(chunk.stream().map(Works::getId).toList());
            List<WorksDocument> documents = chunk.stream()
                    .map(works -> WorksDocument.of(works, nicknames.getOrDefault(works.getId(), List.of())))
                    .toList();
            searchIndexManager.bulkIndex(index, documents, document -> String.valueOf(document.worksId()));

            count += chunk.size();
            lastWorksId = chunk.get(chunk.size() - 1).getId();
        }
    }
}
