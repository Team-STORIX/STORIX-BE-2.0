package com.storix.api.domain.plus.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.plus.service.BoardService;
import com.storix.api.domain.plus.controller.dto.ReaderBoardUploadRequest;
import com.storix.domain.domains.plus.dto.CreateReaderBoardCommand;
import com.storix.domain.domains.image.service.S3CacheHelper;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import com.storix.domain.domains.plus.exception.PlusImageNotExistException;
import com.storix.domain.domains.plus.exception.SpoilerScriptRequiredException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class BoardUseCase {

    private final BoardService boardService;
    private final S3CacheHelper s3CacheHelper;

    public CustomResponse<Void> createReaderBoard(Long userId, ReaderBoardUploadRequest req) {
        if (req.isSpoiler() && (req.spoilerScript() == null || req.spoilerScript().isBlank())) {
            throw SpoilerScriptRequiredException.EXCEPTION;
        }
        if (!req.objectKeys().isEmpty()) {
            if (!s3CacheHelper.isValidBoardKeys(userId, req.objectKeys())) {
                throw PlusImageNotExistException.EXCEPTION;
            }
        }
        String spoilerScript = req.isSpoiler() ? req.spoilerScript() : null;
        CreateReaderBoardCommand cmd = new CreateReaderBoardCommand(
                userId,
                req.isWorksSelected(),
                req.worksId(),
                req.isSpoiler(),
                spoilerScript,
                req.content(),
                req.theme(),
                req.objectKeys()
        );
        boardService.createReaderBoard(cmd);
        // 소비된 키는 유효 키 캐시에서 제거 — 같은 키로 여러 게시글이 같은 S3 오브젝트를 참조하면
        // 한 게시글 삭제 시 다른 게시글의 이미지까지 S3에서 지워지므로 재사용을 차단한다
        s3CacheHelper.evictBoardKeys(userId, req.objectKeys());
        return CustomResponse.onSuccess(SuccessCode.PLUS_BOARD_UPLOAD_SUCCESS);
    }

}