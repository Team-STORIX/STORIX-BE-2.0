package com.storix.domain.domains.feed.adaptor;

import com.storix.common.utils.RedisKeyStatic;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TodayFeedSnapshotAdaptor {

    // 선정일 D 의 결과는 D+1 08시까지 읽힌다. 경계에서 먼저 끊기지 않게 하루를 더 둔다
    private static final Duration TTL = Duration.ofDays(2);

    private static final DateTimeFormatter KEY_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String DELIMITER = ",";

    private final StringRedisTemplate redisTemplate;

    // 선정 결과가 없으면 empty, 후보가 0건이라 비어있는 선정이면 빈 리스트
    public Optional<List<Long>> find(LocalDate selectionDate) {
        String raw = redisTemplate.opsForValue().get(key(selectionDate));
        if (raw == null) {
            return Optional.empty();
        }
        if (raw.isEmpty()) {
            return Optional.of(List.of());
        }
        return Optional.of(Arrays.stream(raw.split(DELIMITER))
                .map(Long::valueOf)
                .toList());
    }

    public void save(LocalDate selectionDate, List<Long> boardIds) {
        String raw = boardIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER));
        redisTemplate.opsForValue().set(key(selectionDate), raw, TTL);
    }

    private String key(LocalDate selectionDate) {
        return RedisKeyStatic.Feed.TODAY_SELECTION_PREFIX + selectionDate.format(KEY_DATE);
    }
}
