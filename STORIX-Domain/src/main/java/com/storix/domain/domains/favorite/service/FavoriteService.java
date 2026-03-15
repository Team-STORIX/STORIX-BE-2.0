package com.storix.domain.domains.favorite.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteArtistAdaptor;
import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.Role;
import com.storix.domain.domains.favorite.exception.DuplicateFavoriteArtistRequestException;
import com.storix.domain.domains.favorite.exception.DuplicateFavoriteWorksRequestException;
import com.storix.domain.domains.favorite.exception.FavoriteArtistNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final FavoriteArtistAdaptor favoriteArtistAdaptor;
    private final UserAdaptor userAdaptor;

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
    }

    @Transactional
    public void deleteFavoriteWorks(Long userId, Long worksId) {
        favoriteWorksAdaptor.deleteSingleFavoriteWorks(userId, worksId);
    }

    // [작품] 관심 작가 관련
    @Transactional(readOnly = true)
    public boolean isFavoriteArtist(Long userId, Long artistId) {

        // 작가 계정 확인
        Role role = userAdaptor.findUserRoleByUserId(artistId);
        if (role.equals(Role.READER)) {
            throw FavoriteArtistNotFoundException.EXCEPTION;
        }

        return favoriteArtistAdaptor.isFavoriteArtistAlreadySelected(userId, artistId);
    }

    @Transactional
    public void saveFavoriteArtist(Long userId, Long artistId) {

        // 작가 계정 확인
        Role role = userAdaptor.findUserRoleByUserId(artistId);
        if (role.equals(Role.READER)) {
            throw FavoriteArtistNotFoundException.EXCEPTION;
        }

        if (favoriteArtistAdaptor.isFavoriteArtistAlreadySelected(userId, artistId)) {
            throw DuplicateFavoriteArtistRequestException.EXCEPTION;
        }

        favoriteArtistAdaptor.saveSingleFavoriteArtist(userId, artistId);
    }

    @Transactional
    public void deleteFavoriteArtist(Long userId, Long artistId) {
        favoriteArtistAdaptor.deleteSingleFavoriteArtist(userId, artistId);
    }

}
