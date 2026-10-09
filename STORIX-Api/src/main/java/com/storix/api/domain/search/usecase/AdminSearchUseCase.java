package com.storix.api.domain.search.usecase;

import com.storix.api.domain.search.controller.dto.WorksNicknameCreateRequest;
import com.storix.api.domain.search.helper.WorksNicknameCsvHelper;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.search.dto.WorksNicknameBulkResponse;
import com.storix.domain.domains.search.dto.WorksNicknameResponse;
import com.storix.domain.domains.search.dto.WorksReindexResponse;
import com.storix.domain.domains.search.service.WorksIndexService;
import com.storix.domain.domains.search.service.WorksNicknameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class AdminSearchUseCase {

    private final WorksIndexService worksIndexService;
    private final WorksNicknameService worksNicknameService;
    private final WorksNicknameCsvHelper worksNicknameCsvHelper;

    // 작품 검색 재색인
    public WorksReindexResponse reindexWorks() {
        return worksIndexService.reindexAll();
    }

    // 작품 별칭 목록 조회
    public List<WorksNicknameResponse> findWorksNicknames(Long worksId) {
        return worksNicknameService.findNicknames(worksId);
    }

    // 작품 별칭 등록
    public WorksNicknameResponse addWorksNickname(Long worksId, WorksNicknameCreateRequest request) {

        // 0. 재색인 중이면 거절
        worksIndexService.checkNotReindexing();

        // 1. 별칭 저장
        WorksNicknameResponse response = worksNicknameService.addNickname(worksId, request.nickname());

        // 2. 커밋된 별칭으로 작품 문서 다시 색인
        worksIndexService.indexWorks(worksId);
        return response;
    }

    // 작품 별칭 CSV 일괄 등록
    public WorksNicknameBulkResponse addWorksNicknamesFromCsv(MultipartFile file) {

        // 0. 재색인 중이면 거절
        worksIndexService.checkNotReindexing();

        // 1. CSV 를 읽어 별칭 저장
        WorksNicknameBulkResponse response = worksNicknameService.addNicknames(worksNicknameCsvHelper.parse(file));

        // 2. 추가된 별칭이 있으면 전체 재색인
        if (response.addedCount() > 0) worksIndexService.reindexAll();
        return response;
    }

    // 작품 별칭 삭제
    public void deleteWorksNickname(Long worksId, Long nicknameId) {

        // 0. 재색인 중이면 거절
        worksIndexService.checkNotReindexing();

        // 1. 별칭 삭제
        worksNicknameService.deleteNickname(worksId, nicknameId);

        // 2. 남은 별칭으로 작품 문서 다시 색인
        worksIndexService.indexWorks(worksId);
    }
}
