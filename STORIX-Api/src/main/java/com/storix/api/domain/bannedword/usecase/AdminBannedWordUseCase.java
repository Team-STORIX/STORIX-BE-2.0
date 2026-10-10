package com.storix.api.domain.bannedword.usecase;

import com.storix.api.domain.bannedword.controller.dto.BannedWordBulkCreateRequest;
import com.storix.api.domain.bannedword.controller.dto.BannedWordCreateRequest;
import com.storix.api.domain.bannedword.helper.BannedWordCsvHelper;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.bannedword.dto.BannedWordPageResponse;
import com.storix.domain.domains.bannedword.service.BannedWordAdminService;
import com.storix.domain.domains.bannedword.service.BannedWordMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


@UseCase
@RequiredArgsConstructor
public class AdminBannedWordUseCase {

    private final BannedWordAdminService bannedWordAdminService;
    private final BannedWordMatcher bannedWordMatcher;
    private final BannedWordCsvHelper bannedWordCsvHelper;

    public BannedWordPageResponse searchBannedWords(String keyword, Pageable pageable) {
        return BannedWordPageResponse.from(bannedWordAdminService.search(keyword, pageable));
    }

    public void addBannedWord(BannedWordCreateRequest request) {
        bannedWordAdminService.addWord(request.word());   // 트랜잭션
        bannedWordMatcher.reload();                        // 비 트랜잭션 (DB 커밋 후 캐시 갱신)
    }

    public void addBannedWords(BannedWordBulkCreateRequest request) {
        bannedWordAdminService.addWords(request.words());
        bannedWordMatcher.reload();
    }

    public void addBannedWordsFromCsv(MultipartFile file) {
        bannedWordAdminService.addWords(bannedWordCsvHelper.parseWords(file));
        bannedWordMatcher.reload();
    }

    public void deleteBannedWord(Long bannedWordId) {
        bannedWordAdminService.deleteWord(bannedWordId);
        bannedWordMatcher.reload();
    }

    public void reloadCache() {
        bannedWordMatcher.reload();
    }
}
