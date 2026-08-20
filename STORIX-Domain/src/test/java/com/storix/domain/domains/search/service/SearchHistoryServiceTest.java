package com.storix.domain.domains.search.service;

import com.storix.common.utils.RedisKeyStatic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[검색] 인기 검색어 점수 적재")
class SearchHistoryServiceTest {

    private static final String KEYWORD = "나 혼자만 레벨업";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private SearchHistoryService searchHistoryService;

    private String todayKey() {
        return RedisKeyStatic.Search.TRENDING_PREFIX
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    @Nested
    @DisplayName("addTrendingScore")
    class AddTrendingScore {

        @BeforeEach
        void stubZSet() {
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        }

        @Test
        @DisplayName("오늘 키에 점수 1을 더한다")
        void increments_today_key() {
            searchHistoryService.addTrendingScore(KEYWORD);

            verify(zSetOperations).incrementScore(todayKey(), KEYWORD, 1.0);
        }

        @Test
        @DisplayName("키 TTL 을 갱신한다")
        void refreshes_ttl() {
            searchHistoryService.addTrendingScore(KEYWORD);

            verify(redisTemplate).expire(eq(todayKey()), anyLong(), any());
        }

        @Test
        @DisplayName("최근 검색어는 남기지 않는다")
        void does_not_touch_recent_list() {
            searchHistoryService.addTrendingScore(KEYWORD);

            verify(redisTemplate, never()).execute(any(), any(), any());
            verify(redisTemplate, never()).opsForList();
        }
    }

    @Nested
    @DisplayName("빈 검색어")
    class BlankKeyword {

        @Test
        @DisplayName("null 이면 아무것도 하지 않는다")
        void null_is_ignored() {
            searchHistoryService.addTrendingScore(null);

            verifyNoInteractions(redisTemplate);
        }

        @Test
        @DisplayName("공백만 있으면 아무것도 하지 않는다")
        void blank_is_ignored() {
            searchHistoryService.addTrendingScore("   ");

            verifyNoInteractions(redisTemplate);
        }
    }
}
