package com.storix.storix_api.domains.favorite.adaptor;

import com.storix.storix_api.domains.favorite.domain.FavoriteWorks;
import com.storix.storix_api.domains.favorite.repository.FavoriteWorksRepository;
import com.storix.storix_api.global.apiPayload.exception.favorite.DuplicateFavoriteWorksRequestException;
import com.storix.storix_api.global.apiPayload.exception.favorite.InvalidFavoriteWorksRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FavoriteWorksAdaptor {

    private final FavoriteWorksRepository favoriteWorksRepository;

    // 온보딩 관심 작품 리스트 저장
    public void saveFavoriteWorks(Long userId, Set<Long> worksIds) {
        List<FavoriteWorks> entities = worksIds.stream()
                .map(worksId -> new FavoriteWorks(userId, worksId))
                .toList();

        favoriteWorksRepository.saveAll(entities);
    }

    // 관심 작품 리스트 제거
    public void deleteFavoriteWorks(Long userId) {
        favoriteWorksRepository.deleteByUserId(userId);
    }

    // 작품 상세 관심 작품 여부 조회
    public boolean isFavoriteWorksAlreadySelected(Long userId, Long worksId) {
        return favoriteWorksRepository.existsByUserIdAndWorksId(userId, worksId);
    }

    // 작품 상세 관심 작품 저장
    public void saveSingleFavoriteWorks(Long userId, Long worksId) {
        try {
            favoriteWorksRepository.saveAndFlush(new FavoriteWorks(userId, worksId));
        } catch (DataIntegrityViolationException e) {
            // 병렬 요청 처리
            throw DuplicateFavoriteWorksRequestException.EXCEPTION;
        }
    }

    // 작품 상세 관심 작품 제거
    public void deleteSingleFavoriteWorks(Long userId, Long worksId) {
        int isDeleted = favoriteWorksRepository.deleteSingleFavoriteWorks(userId, worksId);
        if (isDeleted == 0) {
            throw InvalidFavoriteWorksRequestException.EXCEPTION;
        }
    }

    // 관심 작품 등록수 조회
    public int countFavoriteWorks(Long userId) {
        return favoriteWorksRepository.countByUserId(userId);
    }

    // 관심 작품 리스트 조회
    public Slice<Long> findSliceFavoriteWorksId(Long userId, Pageable pageable) {
        return favoriteWorksRepository.findWorksIdsByUserId(userId, pageable);
    }

    public List<Long> findAllFavoriteWorksIdsByUserId(Long userId) {
        return favoriteWorksRepository.findAllWorksIdsByUserId(userId);
    }
}
