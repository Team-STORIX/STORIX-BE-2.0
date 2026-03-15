package com.storix.storix_api.domains.user.application.service;

import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.adaptor.UserAdaptor;
import com.storix.storix_api.domains.user.domain.User;
import com.storix.storix_api.domains.user.dto.LoginInfo;
import com.storix.storix_api.global.apiPayload.exception.user.ArtistLoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistLoginService implements UserDetailsService {

    private final UserAdaptor userAdaptor;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User artistUser = userAdaptor.findArtistUserByLoginId(username);
        artistUser.login();

        return new AuthUserDetails(artistUser.getId(), artistUser.getRole());
    }

    public void validateArtistLogin (String loginId, String password) {
        LoginInfo artistUserLoginInfo = userAdaptor.findArtistUserLoginInfoByLoginId(loginId);

        if (!passwordEncoder.matches(password, artistUserLoginInfo.password())) {
            throw ArtistLoginException.EXCEPTION;
        }

    }
}
