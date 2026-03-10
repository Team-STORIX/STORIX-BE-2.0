package com.storix.api.domain.plus.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.plus.service.BoardService;
import com.storix.api.domain.plus.controller.dto.ArtistBoardUploadRequest;
import com.storix.api.domain.plus.controller.dto.ReaderBoardUploadRequest;
import com.storix.domain.domains.plus.dto.CreateReaderBoardCommand;
import com.storix.domain.domains.plus.dto.CreateArtistBoardCommand;
import com.storix.domain.domains.image.service.S3CacheHelper;
import com.storix.common.payload.CustomResponse;
import com.storix.common.code.SuccessCode;
import com.storix.domain.domains.plus.exception.PlusImageNotExistException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class BoardUseCase {

    private final BoardService boardService;
    private final S3CacheHelper s3CacheHelper;

    public CustomResponse<Void> createReaderBoard(Long userId, ReaderBoardUploadRequest req) {
        if (!req.objectKeys().isEmpty()) {
            if (!s3CacheHelper.isValidBoardKeys(userId, req.objectKeys())) {
                throw PlusImageNotExistException.EXCEPTION;
            }
        }
        CreateReaderBoardCommand cmd = new CreateReaderBoardCommand(
                userId,
                req.isWorksSelected(),
                req.worksId(),
                req.isSpoiler(),
                req.content(),
                req.objectKeys()
        );
        boardService.createReaderBoard(cmd);
        return CustomResponse.onSuccess(SuccessCode.PLUS_BOARD_UPLOAD_SUCCESS);
    }

    public CustomResponse<Void> createArtistBoard(Long userId, ArtistBoardUploadRequest req) {
        if (!req.objectKeys().isEmpty()) {
            if (req.isContentForFan()) {
                if (!s3CacheHelper.isValidFanContentKeys(userId, req.objectKeys())) {
                    throw PlusImageNotExistException.EXCEPTION;
                }
            } else {
                if (!s3CacheHelper.isValidBoardKeys(userId, req.objectKeys())) {
                    throw PlusImageNotExistException.EXCEPTION;
                }
            }
        }
        CreateArtistBoardCommand cmd = new CreateArtistBoardCommand(
                userId,
                req.isWorksSelected(),
                req.worksId(),
                req.isContentForFan(),
                req.point(),
                req.content(),
                req.objectKeys()
        );
        boardService.createArtistBoard(cmd);
        return CustomResponse.onSuccess(SuccessCode.PLUS_BOARD_UPLOAD_SUCCESS);
    }

}