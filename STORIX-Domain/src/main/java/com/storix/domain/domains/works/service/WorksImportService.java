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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
        if (!Boolean.TRUE.equals(locked)) throw WorksImportInProgressException.EXCEPTION;

        try {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            List<WorksImportResult> results = new ArrayList<>();
            for (WorksImportItem item : items) {
                results.add(importOne(transaction, item));
            }

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
            log.info(">>> [WorksImport] 적재 완료 total={}, changed={}", items.size(), changedWorksIds.size());
            return results;
        } finally {
            redisTemplate.execute(UNLOCK_SCRIPT, List.of(RedisKeyStatic.Works.IMPORT_LOCK), token);
        }
    }

    // 건마다 트랜잭션을 나눠서 한 건이 실패해도 나머지는 저장된다
    private WorksImportResult importOne(TransactionTemplate transaction, WorksImportItem item) {
        try {
            return transaction.execute(status -> upsert(item));
        } catch (Exception e) {
            log.warn(">>> [WorksImport] 작품 적재 실패 stagingId={}, cause={}", item.stagingId(), e.getMessage());
            return WorksImportResult.failed(item.stagingId(), e.getMessage());
        }
    }

    private WorksImportResult upsert(WorksImportItem item) {
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
            return WorksImportResult.of(item.stagingId(), Status.CREATED, saved.getId());
        }

        boolean changed = works.updateFromImport(item.author(), item.illustrator(), item.originalAuthor(),
                ageClassification, genre, worksType, item.description(), item.thumbnailUrl());
        if (platform != null) changed |= works.putPlatform(platform, item.landingUrl());
        if (item.hashtags() != null && !item.hashtags().isEmpty()) changed |= works.replaceHashtags(resolveHashtags(item.hashtags()));
        return WorksImportResult.of(item.stagingId(), changed ? Status.UPDATED : Status.UNCHANGED, works.getId());
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
}
