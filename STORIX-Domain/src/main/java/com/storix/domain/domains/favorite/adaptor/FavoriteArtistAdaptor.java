package com.storix.domain.domains.favorite.adaptor;

import com.storix.domain.domains.favorite.domain.FavoriteArtist;
import com.storix.domain.domains.favorite.repository.FavoriteArtistRepository;
import com.storix.domain.domains.favorite.exception.DuplicateFavoriteArtistRequestException;
import com.storix.domain.domains.favorite.exception.InvalidFavoriteArtistRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteArtistAdaptor {

    private final FavoriteArtistRepository favoriteArtistRepository;

    // 관심 작가 등록 여부 조회
    public boolean isFavoriteArtistAlreadySelected (Long userId, Long artistId) {
        return favoriteArtistRepository.existsByUserIdAndArtistId(userId, artistId);
    }

    // 관심 작가 저장
    public void saveSingleFavoriteArtist(Long userId, Long artistId) {
        try {
            favoriteArtistRepository.saveAndFlush(new FavoriteArtist(userId, artistId));
        } catch (DataIntegrityViolationException e) {
            // 병렬 요청 처리
            throw DuplicateFavoriteArtistRequestException.EXCEPTION;
        }
    }

    // 관심 작가 해제
    public void deleteSingleFavoriteArtist(Long userId, Long artistId) {
        int isDeleted = favoriteArtistRepository.deleteSingleFavoriteArtist(userId, artistId);
        if (isDeleted == 0) {
            throw InvalidFavoriteArtistRequestException.EXCEPTION;
        }
    }

    // 관심 작가 등록수 조회
    public int countFavoriteArtist(Long userId) {
        return favoriteArtistRepository.countByUserId(userId);
    }

    // 관심 작가 리스트 조회
    public Slice<Long> findAllFavoriteArtistsId(Long userId, Pageable pageable) {
        return favoriteArtistRepository.findArtistIdsByUserId(userId, pageable);
    }

}
