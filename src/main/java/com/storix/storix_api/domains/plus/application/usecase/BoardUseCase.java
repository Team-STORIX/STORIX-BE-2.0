package com.storix.storix_api.domains.plus.application.usecase;

import com.storix.storix_api.UseCase;
import com.storix.storix_api.domains.plus.application.service.BoardService;
import com.storix.storix_api.domains.plus.controller.dto.ArtistBoardUploadRequest;
import com.storix.storix_api.domains.plus.controller.dto.ReaderBoardUploadRequest;
import com.storix.storix_api.global.apiPayload.CustomResponse;
import com.storix.storix_api.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class BoardUseCase {

    private final BoardService boardService;

    public CustomResponse<Void> createReaderBoard(Long userId, ReaderBoardUploadRequest req) {
        boardService.createReaderBoard(userId, req);
        return CustomResponse.onSuccess(SuccessCode.PLUS_BOARD_UPLOAD_SUCCESS);
    }

    public CustomResponse<Void> createArtistBoard(Long userId, ArtistBoardUploadRequest req) {
        boardService.createArtistBoard(userId, req);
        return CustomResponse.onSuccess(SuccessCode.PLUS_BOARD_UPLOAD_SUCCESS);
    }

}