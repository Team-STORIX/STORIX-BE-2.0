package com.storix.api.domain.search.usecase;

import com.storix.api.domain.search.controller.dto.WorksNicknameCreateRequest;
import com.storix.api.domain.search.helper.WorksNicknameCsvHelper;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.WorksNicknameBulkResponse;
import com.storix.domain.domains.search.dto.WorksNicknameResponse;
import com.storix.domain.domains.search.dto.SearchReindexResponse;
import com.storix.domain.domains.search.service.FeedIndexService;
import com.storix.domain.domains.search.service.WorksIndexService;
import com.storix.domain.domains.search.service.WorksNicknameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminSearchUseCase {

    private final WorksIndexService worksIndexService;
    private final FeedIndexService feedIndexService;
    private final WorksNicknameService worksNicknameService;
    private final WorksNicknameCsvHelper worksNicknameCsvHelper;

    // 작품 검색 재색인
    public SearchReindexResponse reindexWorks() {
        return worksIndexService.reindexAll();
    }

    // 피드 검색 재색인
    public SearchReindexResponse reindexFeeds() {
        return feedIndexService.reindexAll();
    }

    // 작품 별칭 목록 조회
    public List<WorksNicknameResponse> findWorksNicknames(Long worksId) {
        return worksNicknameService.findNicknames(worksId);
    }

    // 작품 별칭 등록
    public WorksNicknameResponse addWorksNickname(Long worksId, WorksNicknameCreateRequest request) {

        // 0. 재색인 중이면 거절
        worksIndexService.checkNotReindexing();
        feedIndexService.checkNotReindexing();

        // 1. 별칭 저장
        WorksNicknameResponse response = worksNicknameService.addNickname(worksId, request.nickname());

        // 2. 커밋된 별칭으로 작품 · 게시글 문서 다시 색인
        worksIndexService.indexWorks(worksId);
        feedIndexService.indexBoardsOfWorks(List.of(worksId));
        return response;
    }

    // 작품 별칭 CSV 일괄 등록
    public WorksNicknameBulkResponse addWorksNicknamesFromCsv(MultipartFile file) {

        // 0. 재색인 중이면 거절
        worksIndexService.checkNotReindexing();
        feedIndexService.checkNotReindexing();

        // 1. CSV 를 읽어 별칭 저장
        WorksNicknameBulkResponse response = worksNicknameService.addNicknames(worksNicknameCsvHelper.parse(file));

        // 2. 추가된 별칭이 있으면 작품 · 피드 전체 재색인. 작품이 실패해도 피드는 돌린다
        if (response.addedCount() > 0) {
            try {
                worksIndexService.reindexAll();
            } finally {
                feedIndexService.reindexAll();
            }
        }
        return response;
    }

    // 작품 별칭 삭제
    public void deleteWorksNickname(Long worksId, Long nicknameId) {

        // 0. 재색인 중이면 거절
        worksIndexService.checkNotReindexing();
        feedIndexService.checkNotReindexing();

        // 1. 별칭 삭제
        worksNicknameService.deleteNickname(worksId, nicknameId);

        // 2. 남은 별칭으로 작품 · 게시글 문서 다시 색인
        worksIndexService.indexWorks(worksId);
        feedIndexService.indexBoardsOfWorks(List.of(worksId));
    }
}
