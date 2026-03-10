package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    void deleteByRefreshToken(String refreshToken);
}
