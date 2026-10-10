package com.storix.internal.works.usecase;

import com.storix.internal.works.controller.dto.WorksImportRequest;
import com.storix.internal.works.controller.dto.WorksMergeRequest;
import com.storix.internal.works.controller.dto.WorksRenameRequest;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.hashtag.service.HashtagCacheHelper;
import com.storix.domain.domains.onboarding.service.OnboardingWorksHelper;
import com.storix.domain.domains.search.service.WorksIndexService;
import com.storix.domain.domains.works.dto.WorksEnumCatalogResponse;
import com.storix.domain.domains.works.dto.WorksImportResult;
import com.storix.domain.domains.works.dto.WorksMergeResult;
import com.storix.domain.domains.works.dto.WorksRenameResult;
import com.storix.domain.domains.works.exception.WorksImportInProgressException;
import com.storix.domain.domains.works.service.WorksImportLockHelper;
import com.storix.domain.domains.works.service.WorksImportService;
import com.storix.domain.domains.works.service.WorksMergeService;
import com.storix.domain.domains.works.service.WorksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class InternalWorksUseCase {

    private final WorksImportService worksImportService;
    private final WorksIndexService worksIndexService;
    private final HashtagCacheHelper hashtagCacheHelper;
    private final WorksMergeService worksMergeService;
    private final WorksImportLockHelper worksImportLockHelper;
    private final OnboardingWorksHelper onboardingWorksHelper;
    private final WorksService worksService;

    // 작품 enum 카탈로그 조회
    public WorksEnumCatalogResponse getEnumCatalog() {
        return WorksEnumCatalogResponse.create();
    }

    // 검수 통과 작품 적재
    public List<WorksImportResult> importWorks(WorksImportRequest request) {
        List<WorksImportResult> results = worksImportService.importAll(request.toItems());

        // 작품 검색 재색인
        List<Long> changedWorksIds = WorksImportResult.changedWorksIds(results);
        if (!changedWorksIds.isEmpty()) {
            hashtagCacheHelper.evictGlobalMeta();
            worksIndexService.indexWorksBulk(changedWorksIds);
        }
        return results;
    }

    // 중복 작품 병합. 적재와 같은 잠금으로 동시에 돌지 않게 함
    public WorksMergeResult mergeWorks(Long keepWorksId, WorksMergeRequest request) {
        String token = worksImportLockHelper.tryLock().orElseThrow(() -> WorksImportInProgressException.EXCEPTION);
        long startedAt = System.currentTimeMillis();
        WorksMergeResult result;
        try {
            result = worksMergeService.merge(keepWorksId, request.dropWorksIds());
        } finally {
            worksImportLockHelper.unlock(token);
        }

        worksIndexService.indexWorks(keepWorksId);
        worksIndexService.deleteWorks(result.mergedWorksIds());
        hashtagCacheHelper.evictGlobalMeta();
        if (result.onboardingPromoted()) onboardingWorksHelper.evictCache();

        log.atInfo()
                .addKeyValue("keepWorksId", keepWorksId)
                .addKeyValue("dropWorksIds", result.mergedWorksIds())
                .addKeyValue("moved", result.moved())
                .addKeyValue("removedDuplicates", result.removedDuplicates())
                .log(">>> [WorksMerge] 병합 완료 keepWorksId={}, dropWorksIds={}, moved={}, removedDuplicates={}, elapsedMs={}",
                        keepWorksId, result.mergedWorksIds(), result.moved(), result.removedDuplicates(),
                        System.currentTimeMillis() - startedAt);
        return result;
    }

    // 작품명 정정. 적재가 옛 이름으로 같은 작품을 찾지 않게 같은 잠금 사용
    public WorksRenameResult renameWorks(Long worksId, WorksRenameRequest request) {
        String token = worksImportLockHelper.tryLock().orElseThrow(() -> WorksImportInProgressException.EXCEPTION);
        WorksRenameResult result;
        try {
            result = worksService.rename(worksId, request.currentName(), request.newName());
        } finally {
            worksImportLockHelper.unlock(token);
        }

        worksIndexService.indexWorks(worksId);

        log.atInfo()
                .addKeyValue("worksId", worksId)
                .addKeyValue("beforeName", result.beforeName())
                .addKeyValue("afterName", result.afterName())
                .log(">>> [WorksRename] 작품명 변경");
        return result;
    }
}
