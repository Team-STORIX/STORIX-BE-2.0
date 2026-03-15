package com.storix.domain.domains.plus.service;

import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardImageAdaptor;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.dto.CreateReaderBoardCommand;
import com.storix.domain.domains.works.application.helper.AdultWorksHelper;
import com.storix.domain.domains.plus.exception.WorksIdNotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardAdaptor boardAdaptor;
    private final BoardImageAdaptor boardImageAdaptor;
    private final LibraryAdaptor libraryAdaptor;

    private final AdultWorksHelper adultWorksHelper;

    // 독자 게시물 생성
    @Transactional
    public void createReaderBoard(CreateReaderBoardCommand cmd) {

        if (cmd.isWorksSelected()) {
            if (cmd.worksId() == null) {
                throw WorksIdNotExistException.EXCEPTION;
            }
            // 성인 작품 여부 확인 및 핸들링
            adultWorksHelper.CheckUserAuthorityWithWorks(cmd.userId(), cmd.worksId());
        }

        ReaderBoard readerBoard = boardAdaptor.saveReaderBoard(cmd);

        if (!cmd.objectKeys().isEmpty()) {
            boardImageAdaptor.saveReaderBoardImages(readerBoard, cmd.objectKeys());
        }

        libraryAdaptor.incrementBoardCount(cmd.userId());
    }

}
