package com.storix.domain.domains.search.service;

import static com.storix.common.utils.RedisKeyStatic.Search.WORKS_REINDEX_LOCK;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.storix.domain.domains.search.dto.WorksDocument;
import com.storix.domain.domains.search.dto.WorksReindexResponse;
import com.storix.domain.domains.search.exception.SearchReindexFailedException;
import com.storix.domain.domains.search.exception.SearchReindexInProgressException;
import com.storix.domain.domains.search.config.WorksIndexProperties;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Works;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorksIndexService {

    private static final String INDEX_DEFINITION = "elasticsearch/works-index.json";
    private static final DateTimeFormatter INDEX_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int CHUNK_SIZE = 500;
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final ElasticsearchClient client;
    private final WorksIndexProperties worksIndexProperties;
    private final WorksAdaptor worksAdaptor;
    private final StringRedisTemplate redisTemplate;

    public boolean isAliasMissing() throws IOException {
        return !client.indices().existsAlias(a -> a.name(worksIndexProperties.alias())).value();
    }

    // 재색인 도중 바뀐 별칭은 새 인덱스에 빠질 수 있어 그동안은 별칭을 못 바꾸게 한다
    public void checkNotReindexing() {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(WORKS_REINDEX_LOCK))) throw SearchReindexInProgressException.EXCEPTION;
    }

    // 실패해도 DB 는 이미 바뀌었으니 다음 재색인 때 맞춰진다
    public void indexWorks(Long worksId) {
        try {
            Works works = worksAdaptor.findById(worksId);
            List<String> nicknames = worksAdaptor.loadNicknamesByWorksIds(List.of(worksId)).getOrDefault(worksId, List.of());
            WorksDocument document = WorksDocument.of(works, nicknames);
            // 응답 직후 어드민이 바로 검색해 볼 수 있게 반영될 때까지 기다린다
            client.index(i -> i.index(worksIndexProperties.alias()).id(String.valueOf(worksId)).document(document).refresh(Refresh.WaitFor));
        } catch (Exception e) {
            log.warn(">>> [WorksIndex] 작품 색인 실패 worksId={}, cause={}", worksId, e.getMessage());
        }
    }

    // 실패해도 다음 재색인 때 맞춰진다
    public void indexWorksBulk(List<Long> worksIds) {
        try {
            List<Works> works = worksAdaptor.findWorksByIds(worksIds);
            Map<Long, List<String>> nicknames = worksAdaptor.loadNicknamesByWorksIds(worksIds);

            BulkRequest.Builder bulk = new BulkRequest.Builder().index(worksIndexProperties.alias());
            for (Works each : works) {
                WorksDocument document = WorksDocument.of(each, nicknames.getOrDefault(each.getId(), List.of()));
                bulk.operations(op -> op.index(i -> i.id(String.valueOf(document.worksId())).document(document)));
            }

            BulkResponse response = client.bulk(bulk.build());
            if (response.errors()) log.warn(">>> [WorksIndex] 작품 일부 색인 실패 count={}", worksIds.size());
        } catch (Exception e) {
            log.warn(">>> [WorksIndex] 작품 색인 실패 count={}, cause={}", worksIds.size(), e.getMessage());
        }
    }

    public WorksReindexResponse reindexAll() {
        String token = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(WORKS_REINDEX_LOCK, token, LOCK_TTL);
        if (!Boolean.TRUE.equals(locked)) throw SearchReindexInProgressException.EXCEPTION;

        try {
            return reindex();
        } catch (SearchReindexFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error(">>> [WorksIndex] 재색인 실패 cause={}", e.getMessage(), e);
            throw SearchReindexFailedException.EXCEPTION;
        } finally {
            // TTL 이 먼저 지나 다른 재색인이 잡은 잠금은 지우지 않는다
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(WORKS_REINDEX_LOCK), token);
        }
    }

    // alias 는 다 색인한 뒤 한 번에 옮겨서, 도중에 실패해도 검색은 기존 인덱스로 계속된다
    private WorksReindexResponse reindex() throws IOException {
        String newIndex = worksIndexProperties.alias() + "-" + LocalDateTime.now().format(INDEX_SUFFIX);
        createIndex(newIndex);

        long count;
        try {
            count = indexAllWorks(newIndex);
            client.indices().refresh(r -> r.index(newIndex));
            client.indices().updateAliases(u -> u
                    .actions(a -> a.remove(remove -> remove.index(worksIndexProperties.alias() + "-*").alias(worksIndexProperties.alias()).mustExist(false)))
                    .actions(a -> a.add(add -> add.index(newIndex).alias(worksIndexProperties.alias()))));
        } catch (IOException | RuntimeException e) {
            deleteQuietly(newIndex);
            throw e;
        }

        deleteOldIndices(newIndex);
        log.info(">>> [WorksIndex] 재색인 완료 index={}, count={}", newIndex, count);
        return new WorksReindexResponse(newIndex, count);
    }

    private void createIndex(String index) throws IOException {
        try (InputStream definition = new ClassPathResource(INDEX_DEFINITION).getInputStream()) {
            client.indices().create(c -> c.withJson(definition).index(index));
        }
    }

    private long indexAllWorks(String index) throws IOException {
        long count = 0;
        long lastWorksId = 0;

        while (true) {
            List<Works> chunk = worksAdaptor.findWorksChunkAfter(lastWorksId, CHUNK_SIZE);
            if (chunk.isEmpty()) return count;

            Map<Long, List<String>> nicknames = worksAdaptor.loadNicknamesByWorksIds(chunk.stream().map(Works::getId).toList());

            BulkRequest.Builder bulk = new BulkRequest.Builder().index(index);
            for (Works works : chunk) {
                WorksDocument document = WorksDocument.of(works, nicknames.getOrDefault(works.getId(), List.of()));
                bulk.operations(op -> op.index(i -> i.id(String.valueOf(document.worksId())).document(document)));
            }

            BulkResponse response = client.bulk(bulk.build());
            if (response.errors()) {
                String reason = response.items().stream()
                        .map(BulkResponseItem::error)
                        .filter(error -> error != null)
                        .map(error -> error.reason())
                        .findFirst()
                        .orElse("unknown");
                log.error(">>> [WorksIndex] 작품 색인 실패 index={}, reason={}", index, reason);
                throw SearchReindexFailedException.EXCEPTION;
            }

            count += chunk.size();
            lastWorksId = chunk.get(chunk.size() - 1).getId();
        }
    }

    // 이전 인덱스와 중간에 죽은 실행이 남긴 인덱스를 같이 지운다
    private void deleteOldIndices(String currentIndex) throws IOException {
        List<String> oldIndices = client.indices().get(g -> g.index(worksIndexProperties.alias() + "-*")).result().keySet().stream()
                .filter(index -> !index.equals(currentIndex))
                .toList();
        if (oldIndices.isEmpty()) return;

        client.indices().delete(d -> d.index(oldIndices));
    }

    private void deleteQuietly(String index) {
        try {
            client.indices().delete(d -> d.index(index));
        } catch (Exception e) {
            log.warn(">>> [WorksIndex] 실패한 인덱스 삭제 실패 index={}, cause={}", index, e.getMessage());
        }
    }
}
