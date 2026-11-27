package com.storix.api.domain.search.listener;

import com.storix.domain.domains.search.service.FeedIndexService;
import com.storix.domain.domains.search.service.WorksIndexService;
import com.storix.domain.domains.works.service.WorksSyncPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[검색] 기동 시 색인")
class SearchIndexBootstrapListenerTest {

    @Mock
    private WorksIndexService worksIndexService;
    @Mock
    private FeedIndexService feedIndexService;

    @Test
    @DisplayName("dev 는 인덱스가 있어도 기동마다 작품 · 피드를 다시 색인한다")
    void devAlwaysReindexes() throws Exception {
        new SearchIndexBootstrapListener(worksIndexService, feedIndexService, new WorksSyncPolicy("dev")).reindexIfMissing();

        verify(worksIndexService, timeout(2000)).reindexAll();
        verify(feedIndexService, timeout(2000)).reindexAll();
    }

    @Test
    @DisplayName("prod 는 인덱스가 있으면 다시 색인하지 않는다")
    void prodSkipsWhenAliasExists() throws Exception {
        given(worksIndexService.isAliasMissing()).willReturn(false);
        given(feedIndexService.isAliasMissing()).willReturn(false);

        new SearchIndexBootstrapListener(worksIndexService, feedIndexService, new WorksSyncPolicy("prod")).reindexIfMissing();

        verify(worksIndexService, timeout(2000)).isAliasMissing();
        verify(feedIndexService, timeout(2000)).isAliasMissing();
        verify(worksIndexService, after(300).never()).reindexAll();
        verify(feedIndexService, after(300).never()).reindexAll();
    }
}
