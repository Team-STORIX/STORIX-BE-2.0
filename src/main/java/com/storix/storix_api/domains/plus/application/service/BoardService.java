package com.storix.storix_api.domains.plus.application.service;

import com.storix.storix_api.domains.image.helper.S3CacheHelper;
import com.storix.storix_api.domains.library.adaptor.LibraryAdaptor;
import com.storix.storix_api.domains.plus.adaptor.BoardAdaptor;
import com.storix.storix_api.domains.plus.adaptor.BoardImageAdaptor;
import com.storix.storix_api.domains.plus.controller.dto.ArtistBoardUploadRequest;
import com.storix.storix_api.domains.plus.controller.dto.ReaderBoardUploadRequest;
import com.storix.storix_api.domains.plus.domain.ArtistBoard;
import com.storix.storix_api.domains.plus.domain.ReaderBoard;
import com.storix.storix_api.domains.plus.dto.CreateArtistBoardCommand;
import com.storix.storix_api.domains.plus.dto.CreateReaderBoardCommand;
import com.storix.storix_api.domains.works.application.helper.AdultWorksHelper;
import com.storix.storix_api.domains.works.application.port.LoadWorksPort;
import com.storix.storix_api.global.apiPayload.exception.plus.PlusImageNotExistException;
import com.storix.storix_api.global.apiPayload.exception.plus.WorksIdNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardAdaptor boardAdaptor;
    private final BoardImageAdaptor boardImageAdaptor;
    private final LibraryAdaptor libraryAdaptor;

    private final LoadWorksPort loadWorksPort;

    private final AdultWorksHelper adultWorksHelper;
    private final S3CacheHelper s3CacheHelper;

    // 독자 게시물 생성
    @Transactional
    public void createReaderBoard(Long userId, ReaderBoardUploadRequest req) {

        Long worksId = null;

        if (req.isWorksSelected()) {
            if (req.worksId() == null) {
                throw WorksIdNotExistException.EXCEPTION;
            }

            // 성인 작품 여부 확인 및 핸들링
            adultWorksHelper.CheckUserAuthorityWithWorks(userId, req.worksId());
            worksId = req.worksId();
        }

        if (!req.objectKeys().isEmpty()) {
            if (!s3CacheHelper.isValidBoardKeys(userId, req.objectKeys())) {
                throw PlusImageNotExistException.EXCEPTION;
            }
        }

        CreateReaderBoardCommand cmd = new CreateReaderBoardCommand(
                userId,
                req.isWorksSelected(),
                worksId,
                req.isSpoiler(),
                req.content(),
                req.objectKeys()
        );

        ReaderBoard readerBoard = boardAdaptor.saveReaderBoard(cmd);

        if (!req.objectKeys().isEmpty()) {
            boardImageAdaptor.saveReaderBoardImages(readerBoard, req.objectKeys());
        }

        libraryAdaptor.incrementBoardCount(userId);
    }

    @Transactional
    public void createArtistBoard(Long userId, ArtistBoardUploadRequest req) {

        Long worksId = null;

        if (req.isWorksSelected()) {
            if (req.worksId() == null) {
                throw WorksIdNotExistException.EXCEPTION;
            }
            loadWorksPort.checkWorksExistById(req.worksId());

            worksId = req.worksId();
        }

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
                worksId,
                req.isContentForFan(),
                req.point(),
                req.content(),
                req.objectKeys()
        );

        ArtistBoard artistBoard = boardAdaptor.saveArtistBoard(cmd);

        if (!req.objectKeys().isEmpty()) {
            boardImageAdaptor.saveArtistBoardImages(artistBoard, req.objectKeys());
        }
    }
}
