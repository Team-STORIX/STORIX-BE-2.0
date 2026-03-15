package com.storix.domain.domains.library.adaptor;

import com.storix.domain.domains.library.domain.Library;
import com.storix.domain.domains.library.repository.LibraryRepository;
import com.storix.domain.domains.feed.exception.InvalidReviewRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LibraryAdaptor {

    private final LibraryRepository libraryRepository;

    // 리뷰 개수 업데이트
    public void incrementReviewCount(Long libraryUserId) {
        libraryRepository.incrementReviewCount(libraryUserId);
    }

    public void decrementReviewCount(Long libraryUserId) {
        int isDeleted = libraryRepository.decrementReviewCount(libraryUserId);
        if (isDeleted == 0) {
            throw InvalidReviewRequestException.EXCEPTION;
        }
    }

    // 게시물 개수 업데이트
    public void incrementBoardCount(Long libraryUserId) {
        libraryRepository.incrementBoardCount(libraryUserId);
    }

    public void decrementBoardCount(Long libraryUserId) { libraryRepository.decrementBoardCount(libraryUserId);
    }

    // 서재 업데이트
    public void initLibrary(Long userId) {
        libraryRepository.save(new Library(userId));
    }

    public void deleteLibrary(Long userId) {
        libraryRepository.deleteById(userId);
    }

    // 서재 정보 조회
    public int findReviewCount(Long userId) {
        return libraryRepository.findReviewCountByUserId(userId);
    }

}
