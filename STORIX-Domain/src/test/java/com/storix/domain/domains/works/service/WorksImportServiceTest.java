package com.storix.domain.domains.works.service;

import com.storix.domain.domains.hashtag.adaptor.HashtagAdaptor;
import com.storix.domain.domains.search.adaptor.WorksSearchAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.WorksImportItem;
import com.storix.domain.domains.works.dto.WorksImportResult;
import com.storix.domain.domains.works.dto.WorksImportResult.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[작품] 적재 - 새 작품 생성")
class WorksImportServiceTest {

    @Mock
    private WorksAdaptor worksAdaptor;
    @Mock
    private HashtagAdaptor hashtagAdaptor;
    @Mock
    private WorksSearchAdaptor worksSearchAdaptor;
    @Mock
    private WorksImportLockHelper worksImportLockHelper;
    @Mock
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        given(worksImportLockHelper.tryLock()).willReturn(Optional.of("token"));
        given(worksAdaptor.findWithoutNormalizedNameAfter(anyLong(), anyInt())).willReturn(List.of());
        given(worksAdaptor.findByNormalizedNameAndWorksType(any(), any())).willReturn(List.of());
    }

    private WorksImportService service(String profile) {
        return new WorksImportService(worksAdaptor, hashtagAdaptor, worksSearchAdaptor, worksImportLockHelper,
                transactionManager, new WorksSyncPolicy(profile));
    }

    private static WorksImportItem newItem() {
        return new WorksImportItem(1L, "새 작품", "작가", "작가", null, null, "AGE_15", "ROFAN", "WEBTOON",
                "KAKAO_PAGE", "https://page.kakao.com/content/1", "설명", "https://img/1.jpg", List.of(),
                null, true);
    }

    @Test
    @DisplayName("dev 는 새 작품을 만들지 않고 실패로 돌려준다")
    void devBlocksCreation() {
        List<WorksImportResult> results = service("dev").importAll(List.of(newItem()));

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.result()).isEqualTo(Status.FAILED);
            assertThat(result.error()).contains("prod");
        });
        verify(worksAdaptor, never()).save(any());
    }

    @Test
    @DisplayName("prod · local 은 새 작품을 만든다")
    void otherProfilesCreate() {
        given(hashtagAdaptor.findOrCreateAll(any())).willReturn(Set.of());
        given(worksAdaptor.save(any())).willAnswer(invocation -> {
            Works works = invocation.getArgument(0);
            ReflectionTestUtils.setField(works, "id", 100L);
            return works;
        });

        for (String profile : List.of("prod", "local")) {
            assertThat(service(profile).importAll(List.of(newItem())))
                    .singleElement()
                    .extracting(WorksImportResult::result)
                    .isEqualTo(Status.CREATED);
        }
    }
}
