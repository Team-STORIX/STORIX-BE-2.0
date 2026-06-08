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
        return CustomResponse.onSuccess(SuccessCode.PLUS_BOARD_UPLOAD_SUCCESS);
    }

}