package com.storix.domain.domains.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.storix.domain.domains.search.exception.SearchReindexFailedException;
import com.storix.domain.domains.search.exception.SearchReindexInProgressException;
import com.storix.domain.domains.search.config.WorksIndexProperties;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static com.storix.common.utils.RedisKeyStatic.Search.WORKS_REINDEX_LOCK;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("[검색] 작품 재색인 - 동시 실행 잠금")
class WorksIndexServiceTest {

    @Mock
    private ElasticsearchClient client;
    @Mock
    private WorksAdaptor worksAdaptor;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private WorksIndexService worksIndexService;

    @BeforeEach
    void setUp() {
        worksIndexService = new WorksIndexService(client, new WorksIndexProperties("test"), worksAdaptor, redisTemplate);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("다른 재색인이 돌고 있으면 ES 를 건드리지 않고 거절한다")
    void rejectWhenLocked() {
        given(valueOperations.setIfAbsent(eq(WORKS_REINDEX_LOCK), anyString(), any(Duration.class))).willReturn(false);

        assertThatThrownBy(() -> worksIndexService.reindexAll())
                .isSameAs(SearchReindexInProgressException.EXCEPTION);

        verifyNoInteractions(client);
        verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), any());
    }

    @Test
    @DisplayName("재색인이 실패해도 잡을 때 쓴 토큰으로 잠금을 푼다")
    void releaseLockOnFailure() {
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        given(valueOperations.setIfAbsent(eq(WORKS_REINDEX_LOCK), token.capture(), any(Duration.class))).willReturn(true);
        given(client.indices()).willThrow(new IllegalStateException("ES 연결 실패"));

        assertThatThrownBy(() -> worksIndexService.reindexAll())
                .isSameAs(SearchReindexFailedException.EXCEPTION);

        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(WORKS_REINDEX_LOCK)), eq(token.getValue()));
        verify(redisTemplate, never()).delete(anyString());
    }
}
