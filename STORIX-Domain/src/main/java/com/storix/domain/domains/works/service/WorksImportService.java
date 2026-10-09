package com.storix.domain.domains.works.service;

import com.storix.common.utils.RedisKeyStatic;
import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.hashtag.repository.HashtagRepository;
import com.storix.domain.domains.hashtag.service.HashtagCacheHelper;
import com.storix.domain.domains.search.service.WorksIndexService;
import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksImportItem;
import com.storix.domain.domains.works.dto.WorksImportResult;
import com.storix.domain.domains.works.dto.WorksImportResult.Status;
import com.storix.domain.domains.works.exception.WorksImportInProgressException;
import com.storix.domain.domains.works.repository.WorksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorksImportService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(10);
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final WorksRepository worksRepository;
    private final HashtagRepository hashtagRepository;
    private final HashtagCacheHelper hashtagCacheHelper;
    private final WorksIndexService worksIndexService;
    private final StringRedisTemplate redisTemplate;
    private final PlatformTransactionManager transactionManager;

    // 같은 작품이 두 요청에서 동시에 INSERT 되지 않도록 적재는 한 번에 하나만 돈다
    public List<WorksImportResult> importAll(List<WorksImportItem> items) {
        String token = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(RedisKeyStatic.Works.IMPORT_LOCK, token, LOCK_TTL);
        if (!Boolean.TRUE.equals(locked)) {
            log.warn(">>> [WorksImport] 적재 중이라 거절 total={}", items.size());
            throw WorksImportInProgressException.EXCEPTION;
        }

        long startedAt = System.currentTimeMillis();
        try {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            List<ImportOutcome> outcomes = new ArrayList<>();
            for (WorksImportItem item : items) {
                outcomes.add(importOne(transaction, item));
            }
            List<WorksImportResult> results = outcomes.stream().map(ImportOutcome::result).toList();

            // 잠금을 쥔 채 색인해야 다음 적재가 같은 작품을 바꾼 뒤 이전 값으로 덮어쓰지 않는다
            List<Long> changedWorksIds = results.stream()
                    .filter(WorksImportResult::changed)
                    .map(WorksImportResult::worksId)
                    .distinct()
                    .toList();
            if (!changedWorksIds.isEmpty()) {
                hashtagCacheHelper.evictGlobalMeta();
                worksIndexService.indexWorksBulk(changedWorksIds);
            }
            Map<Status, Long> counts = results.stream().collect(Collectors.groupingBy(WorksImportResult::result, Collectors.counting()));
            long failed = counts.getOrDefault(Status.FAILED, 0L);
            // 건별 결과는 한 줄의 items 필드에 담는다. 100건이어도 로그는 한 줄이다
            (failed > 0 ? log.atWarn() : log.atInfo())
                    .addKeyValue("items", outcomes.stream().map(ImportOutcome::toLogItem).toList())
                    .log(">>> [WorksImport] 적재 완료 total={}, created={}, updated={}, unchanged={}, failed={}, elapsedMs={}",
                            items.size(), counts.getOrDefault(Status.CREATED, 0L), counts.getOrDefault(Status.UPDATED, 0L),
                            counts.getOrDefault(Status.UNCHANGED, 0L), failed, System.currentTimeMillis() - startedAt);
            return results;
        } finally {
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(RedisKeyStatic.Works.IMPORT_LOCK), token);
        }
    }

    // 건마다 트랜잭션을 나눠서 한 건이 실패해도 나머지는 저장된다
    private ImportOutcome importOne(TransactionTemplate transaction, WorksImportItem item) {
        try {
            return transaction.execute(status -> upsert(item));
        } catch (Exception e) {
            return new ImportOutcome(WorksImportResult.failed(item.stagingId(), e.getMessage()),
                    item.worksName(), item.artistName(), List.of());
        }
    }

    private ImportOutcome upsert(WorksImportItem item) {
        String worksName = requireText(item.worksName(), "worksName");
        String artistName = requireText(item.artistName(), "artistName");
        AgeClassification ageClassification = parse(AgeClassification.class, item.ageClassification(), "ageClassification");
        Genre genre = parse(Genre.class, item.genre(), "genre");
        WorksType worksType = parse(WorksType.class, item.worksType(), "worksType");
        Platform platform = parse(Platform.class, item.platform(), "platform");

        Works works = worksRepository.findFirstByWorksNameAndArtistNameOrderByIdAsc(worksName, artistName).orElse(null);
        if (works == null) {
            works = Works.builder()
                    .worksName(worksName)
                    .artistName(artistName)
                    .author(item.author())
                    .illustrator(item.illustrator())
                    .originalAuthor(item.originalAuthor())
                    .ageClassification(require(ageClassification, "ageClassification"))
                    .genre(require(genre, "genre"))
                    .worksType(require(worksType, "worksType"))
                    .description(requireText(item.description(), "description"))
                    .thumbnailUrl(requireText(item.thumbnailUrl(), "thumbnailUrl"))
                    .build();
            if (platform != null) works.putPlatform(platform, item.landingUrl());
            works.replaceHashtags(resolveHashtags(item.hashtags()));
            Works saved = worksRepository.save(works);
            return new ImportOutcome(WorksImportResult.of(item.stagingId(), Status.CREATED, saved.getId()),
                    worksName, artistName, List.of());
        }

        List<String> changes = new ArrayList<>(works.updateFromImport(item.author(), item.illustrator(), item.originalAuthor(),
                ageClassification, genre, worksType, item.description(), item.thumbnailUrl()));
        String platformChange = platform == null ? null : works.putPlatform(platform, item.landingUrl());
        if (platformChange != null) changes.add(platformChange);
        String hashtagChange = item.hashtags() == null || item.hashtags().isEmpty() ? null : works.replaceHashtags(resolveHashtags(item.hashtags()));
        if (hashtagChange != null) changes.add(hashtagChange);

        Status status = changes.isEmpty() ? Status.UNCHANGED : Status.UPDATED;
        return new ImportOutcome(WorksImportResult.of(item.stagingId(), status, works.getId()),
                worksName, artistName, changes);
    }

    private Set<Hashtag> resolveHashtags(List<String> names) {
        if (names == null) return new HashSet<>();

        Set<Hashtag> hashtags = new HashSet<>();
        names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .forEach(name -> hashtags.add(hashtagRepository.findByName(name)
                        .orElseGet(() -> hashtagRepository.save(new Hashtag(name)))));
        return hashtags;
    }

    // enum 을 이름 문자열로 받아 여기서 바꿔야 잘못된 값이 그 건만 실패시킨다
    private static <E extends Enum<E>> E parse(Class<E> type, String name, String field) {
        if (name == null || name.isBlank()) return null;
        try {
            return Enum.valueOf(type, name.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " 값이 enum 카탈로그에 없습니다: " + name);
        }
    }

    private static <T> T require(T value, String field) {
        if (value == null) throw new IllegalArgumentException("신규 작품에는 " + field + " 값이 필요합니다");
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " 값이 비어 있습니다");
        return value.trim();
    }

    private record ImportOutcome(WorksImportResult result, String worksName, String artistName, List<String> changes) {

        Map<String, Object> toLogItem() {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stagingId", result.stagingId());
            item.put("result", result.result());
            if (result.worksId() != null) item.put("worksId", result.worksId());
            item.put("worksName", worksName);
            item.put("artistName", artistName);
            if (!changes.isEmpty()) item.put("changes", changes);
            if (result.error() != null) item.put("error", result.error());
            return item;
        }
    }
}
