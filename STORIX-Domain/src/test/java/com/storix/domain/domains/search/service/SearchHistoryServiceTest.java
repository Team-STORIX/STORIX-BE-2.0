package com.storix.domain.domains.search.service;

import com.storix.common.utils.RedisKeyStatic;
import com.storix.domain.domains.search.adaptor.WorksSearchAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksType;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[검색] 인기 검색어 점수 적재")
class SearchHistoryServiceTest {

    private static final String KEYWORD = "나혼렙";
    private static final String WORKS_NAME = "나 혼자만 레벨업";
    private static final Long WORKS_ID = 1L;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private WorksSearchAdaptor worksSearchAdaptor;

    @Mock
    private WorksAdaptor worksAdaptor;

    @InjectMocks
    private SearchHistoryService searchHistoryService;

    private String todayKey() {
        return RedisKeyStatic.Search.TRENDING_PREFIX
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private void bestMatch(AgeClassification ageClassification) {
        Works works = Works.builder()
                .worksName(WORKS_NAME)
                .artistName("추공")
                .ageClassification(ageClassification)
                .genre(Genre.FANTASY)
                .worksType(WorksType.WEBTOON)
                .build();
        when(worksSearchAdaptor.findBestMatchId(KEYWORD, null, null)).thenReturn(Optional.of(WORKS_ID));
        when(worksAdaptor.findWorksByIds(List.of(WORKS_ID))).thenReturn(List.of(works));
    }

    @Nested
    @DisplayName("점수 적재")
    class AddTrendingScore {

        @BeforeEach
        void stubZSet() {
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        }

        @Test
        @DisplayName("검색어 대신 가장 관련 높은 작품명에 점수 1을 더한다")
        void increments_best_match_works_name() {
            bestMatch(AgeClassification.AGE_15);

            searchHistoryService.addTrendingScore(KEYWORD, null, null);

            verify(zSetOperations).incrementScore(todayKey(), WORKS_NAME, 1.0);
            verify(redisTemplate).expire(eq(todayKey()), anyLong(), any());
        }

        @Test
        @DisplayName("성인 작품도 작품명으로 올린다")
        void adult_works_is_included() {
            bestMatch(AgeClassification.AGE_18);

            searchHistoryService.addTrendingScore(KEYWORD, null, null);

            verify(zSetOperations).incrementScore(todayKey(), WORKS_NAME, 1.0);
        }

        @Test
        @DisplayName("해시태그 검색어는 그대로 올린다")
        void hashtag_is_kept() {
            searchHistoryService.addTrendingScore("#판타지", null, null);

            verify(zSetOperations).incrementScore(todayKey(), "#판타지", 1.0);
            verifyNoInteractions(worksSearchAdaptor);
        }

        @Test
        @DisplayName("최근 검색어는 남기지 않는다")
        void does_not_touch_recent_list() {
            bestMatch(AgeClassification.AGE_15);

            searchHistoryService.addTrendingScore(KEYWORD, null, null);

            verify(redisTemplate, never()).execute(any(), any(), any());
            verify(redisTemplate, never()).opsForList();
        }
    }

    @Nested
    @DisplayName("올리지 않는 경우")
    class Skipped {

        @Test
        @DisplayName("관련 작품 없음")
        void no_match_is_skipped() {
            when(worksSearchAdaptor.findBestMatchId(KEYWORD, null, null)).thenReturn(Optional.empty());

            searchHistoryService.addTrendingScore(KEYWORD, null, null);

            verifyNoInteractions(redisTemplate);
        }

        @Test
        @DisplayName("null · 공백 검색어")
        void blank_is_ignored() {
            searchHistoryService.addTrendingScore(null, null, null);
            searchHistoryService.addTrendingScore("   ", null, null);

            verifyNoInteractions(redisTemplate, worksSearchAdaptor);
        }
    }
}
