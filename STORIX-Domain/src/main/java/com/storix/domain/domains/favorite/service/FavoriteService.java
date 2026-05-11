package com.storix.domain.domains.favorite.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.favorite.exception.DuplicateFavoriteWorksRequestException;
import com.storix.domain.domains.genrescore.event.GenreScoreEventType;
import com.storix.domain.domains.genrescore.publisher.GenreScorePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final GenreScorePublisher genreScorePublisher;

    // [작품] 관심 작품 관련
    @Transactional(readOnly = true)
    public boolean isFavoriteWorks(Long userId, Long worksId) {
        return favoriteWorksAdaptor.isFavoriteWorksAlreadySelected(userId, worksId);
    }

    @Transactional
    public void saveFavoriteWorks(Long userId, Long worksId) {

        if (favoriteWorksAdaptor.isFavoriteWorksAlreadySelected(userId, worksId)) {
            throw DuplicateFavoriteWorksRequestException.EXCEPTION;
        }

        favoriteWorksAdaptor.saveSingleFavoriteWorks(userId, worksId);
        genreScorePublisher.publish(userId, worksId, GenreScoreEventType.FAVORITE_WORKS_ADD);
    }

    @Transactional
    public void deleteFavoriteWorks(Long userId, Long worksId) {
        favoriteWorksAdaptor.deleteSingleFavoriteWorks(userId, worksId);
    }

}
