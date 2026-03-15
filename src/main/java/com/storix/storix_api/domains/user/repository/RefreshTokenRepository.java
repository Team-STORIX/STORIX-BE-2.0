package com.storix.storix_api.domains.user.repository;

import com.storix.storix_api.domains.user.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    void deleteByRefreshToken(String refreshToken);
}
