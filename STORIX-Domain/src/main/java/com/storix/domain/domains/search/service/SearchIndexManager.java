package com.storix.domain.domains.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.storix.domain.domains.search.dto.SearchReindexResponse;
import com.storix.domain.domains.search.exception.SearchReindexFailedException;
import com.storix.domain.domains.search.exception.SearchReindexInProgressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexManager {

    private static final DateTimeFormatter INDEX_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final ElasticsearchClient client;
    private final StringRedisTemplate redisTemplate;

    public boolean isAliasMissing(String alias) throws IOException {
        return !client.indices().existsAlias(a -> a.name(alias)).value();
    }

    public boolean isReindexing(String lockKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public SearchReindexResponse reindex(String alias, String indexDefinition, String lockKey, IndexWriter writer) {
        String token = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, token, LOCK_TTL);
        if (!Boolean.TRUE.equals(locked)) throw SearchReindexInProgressException.EXCEPTION;

        try {
            return switchToNewIndex(alias, indexDefinition, writer);
        } catch (SearchReindexFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error(">>> [SearchIndex] 재색인 실패 alias={}, cause={}", alias, e.getMessage(), e);
            throw SearchReindexFailedException.EXCEPTION;
        } finally {
            // TTL 이 먼저 지나 다른 재색인이 잡은 잠금은 지우지 않는다
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
        }
    }

    public <T> void bulkIndex(String index, List<T> documents, Function<T, String> idOf) throws IOException {
        if (documents.isEmpty()) return;

        BulkRequest.Builder bulk = new BulkRequest.Builder().index(index);
        for (T document : documents) {
            bulk.operations(op -> op.index(i -> i.id(idOf.apply(document)).document(document)));
        }

        BulkResponse response = client.bulk(bulk.build());
        if (response.errors()) {
            String reason = response.items().stream()
                    .map(BulkResponseItem::error)
                    .filter(Objects::nonNull)
                    .map(error -> error.reason())
                    .findFirst()
                    .orElse("unknown");
            log.error(">>> [SearchIndex] 문서 색인 실패 index={}, reason={}", index, reason);
            throw SearchReindexFailedException.EXCEPTION;
        }
    }

    // alias 는 다 색인한 뒤 한 번에 옮겨서, 도중에 실패해도 검색은 기존 인덱스로 계속된다
    private SearchReindexResponse switchToNewIndex(String alias, String indexDefinition, IndexWriter writer) throws IOException {
        String newIndex = alias + "-" + LocalDateTime.now().format(INDEX_SUFFIX);
        createIndex(newIndex, indexDefinition);

        long count;
        try {
            count = writer.writeAll(newIndex);
            client.indices().refresh(r -> r.index(newIndex));
            client.indices().updateAliases(u -> u
                    .actions(a -> a.remove(remove -> remove.index(alias + "-*").alias(alias).mustExist(false)))
                    .actions(a -> a.add(add -> add.index(newIndex).alias(alias))));
        } catch (IOException | RuntimeException e) {
            deleteQuietly(newIndex);
            throw e;
        }

        deleteOldIndices(alias, newIndex);
        log.info(">>> [SearchIndex] 재색인 완료 index={}, count={}", newIndex, count);
        return new SearchReindexResponse(newIndex, count);
    }

    private void createIndex(String index, String indexDefinition) throws IOException {
        try (InputStream definition = new ClassPathResource(indexDefinition).getInputStream()) {
            client.indices().create(c -> c.withJson(definition).index(index));
        }
    }

    // 이전 인덱스와 중간에 죽은 실행이 남긴 인덱스를 같이 지운다
    private void deleteOldIndices(String alias, String currentIndex) throws IOException {
        List<String> oldIndices = client.indices().get(g -> g.index(alias + "-*")).result().keySet().stream()
                .filter(index -> !index.equals(currentIndex))
                .toList();
        if (oldIndices.isEmpty()) return;

        client.indices().delete(d -> d.index(oldIndices));
    }

    private void deleteQuietly(String index) {
        try {
            client.indices().delete(d -> d.index(index));
        } catch (Exception e) {
            log.warn(">>> [SearchIndex] 실패한 인덱스 삭제 실패 index={}, cause={}", index, e.getMessage());
        }
    }

    @FunctionalInterface
    public interface IndexWriter {
        long writeAll(String index) throws IOException;
    }
}
