package com.storix.domain.domains.works.service;

import com.storix.domain.domains.hashtag.adaptor.HashtagAdaptor;
import com.storix.domain.domains.search.adaptor.WorksSearchAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksIdentity;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksImportCommand;
import com.storix.domain.domains.works.dto.WorksImportItem;
import com.storix.domain.domains.works.dto.WorksImportOutcome;
import com.storix.domain.domains.works.dto.WorksImportResult;
import com.storix.domain.domains.works.dto.WorksImportResult.Status;
import com.storix.domain.domains.works.exception.WorksImportInProgressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorksImportService {

    private static final int BACKFILL_CHUNK_SIZE = 500;
    private static final int SIMILAR_CANDIDATE_SIZE = 20;

    private final WorksAdaptor worksAdaptor;
    private final HashtagAdaptor hashtagAdaptor;
    private final WorksSearchAdaptor worksSearchAdaptor;
    private final WorksImportLockHelper worksImportLockHelper;
    private final PlatformTransactionManager transactionManager;

    public List<WorksImportResult> importAll(List<WorksImportItem> items) {
        String token = worksImportLockHelper.tryLock().orElseThrow(() -> {
            log.warn(">>> [WorksImport] 적재 중이라 거절 total={}", items.size());
            return WorksImportInProgressException.EXCEPTION;
        });

        long startedAt = System.currentTimeMillis();
        try {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            // 1. 비교용 제목 없는 기존 작품 채우기
            backfillNormalizedNames(transaction);

            // 2. 적재
            List<WorksImportOutcome> outcomes = items.stream()
                    .map(item -> importOne(transaction, item))
                    .toList();

            // 3. 건별 결과는 items 필드 한 줄로
            Map<Status, Long> counts = WorksImportOutcome.countByStatus(outcomes);
            (counts.containsKey(Status.FAILED) ? log.atWarn() : log.atInfo())
                    .addKeyValue("counts", counts)
                    .addKeyValue("items", outcomes.stream().map(WorksImportOutcome::toLogItem).toList())
                    .log(">>> [WorksImport] 적재 완료 total={}, counts={}, elapsedMs={}",
                            items.size(), counts, System.currentTimeMillis() - startedAt);
            return outcomes.stream().map(WorksImportOutcome::result).toList();
        } finally {
            worksImportLockHelper.unlock(token);
        }
    }

    private void backfillNormalizedNames(TransactionTemplate transaction) {
        long lastWorksId = 0;
        while (true) {
            long after = lastWorksId;
            List<Works> chunk = transaction.execute(status -> {
                List<Works> works = worksAdaptor.findWithoutNormalizedNameAfter(after, BACKFILL_CHUNK_SIZE);
                works.forEach(Works::refreshNormalizedName);
                return works;
            });
            if (chunk == null || chunk.isEmpty()) return;
            lastWorksId = chunk.get(chunk.size() - 1).getId();
            log.info(">>> [WorksImport] 비교용 제목 채움 count={}, lastWorksId={}", chunk.size(), lastWorksId);
        }
    }

    // 건별 트랜잭션. 한 건이 실패해도 나머지는 저장
    private WorksImportOutcome importOne(TransactionTemplate transaction, WorksImportItem item) {
        try {
            return transaction.execute(status -> upsert(item));
        } catch (Exception e) {
            return WorksImportOutcome.failed(item, e.getMessage());
        }
    }

    private WorksImportOutcome upsert(WorksImportItem item) {
        WorksImportCommand command = WorksImportCommand.from(item);

        // 1. 사람이 고른 작품
        if (item.targetWorksId() != null) {
            return update(command, worksAdaptor.findById(item.targetWorksId()));
        }

        // 2. 웹소설이 있는 단행본은 건너뜀
        if (command.isBookEdition()) {
            Works novel = findSameWorks(command.bookBaseKey(), WorksType.WEBNOVEL, command.artists());
            if (novel != null) return WorksImportOutcome.notCreated(command, Status.SKIPPED, List.of(novel.getId()));
        }

        // 3. 같은 작품이면 갱신
        Works same = findSameWorks(command.titleKey(), command.worksType(), command.artists());
        if (same != null) return update(command, same);

        // 4. 중복 의심이면 후보만 반환
        if (!item.createNew()) {
            List<Long> similarIds = worksSearchAdaptor.findSimilarIds(command.titleKey(), command.worksType(), SIMILAR_CANDIDATE_SIZE)
                    .orElse(List.of());
            List<Long> suspects = worksAdaptor.findWorksByIds(similarIds).stream()
                    .filter(works -> WorksIdentity.sharesArtist(command.artists(), works))
                    .map(Works::getId)
                    .sorted()
                    .toList();
            if (!suspects.isEmpty()) return WorksImportOutcome.notCreated(command, Status.SUSPECTED_DUPLICATE, suspects);
        }

        // 5. 새 작품
        Works saved = worksAdaptor.save(command.toNewWorks(hashtagAdaptor.findOrCreateAll(command.hashtagNames())));
        return WorksImportOutcome.created(command, saved.getId());
    }

    private WorksImportOutcome update(WorksImportCommand command, Works works) {
        List<String> changes = command.applyTo(works, hashtagAdaptor.findOrCreateAll(command.hashtagNames()));
        return WorksImportOutcome.updated(command, works.getId(), changes);
    }

    // 제목 · 유형이 같고 작가가 겹치면 같은 작품
    private Works findSameWorks(String titleKey, WorksType worksType, Set<String> artists) {
        if (titleKey.isEmpty()) return null;
        return worksAdaptor.findByNormalizedNameAndWorksType(titleKey, worksType).stream()
                .filter(works -> WorksIdentity.sharesArtist(artists, works))
                .findFirst()
                .orElse(null);
    }
}
