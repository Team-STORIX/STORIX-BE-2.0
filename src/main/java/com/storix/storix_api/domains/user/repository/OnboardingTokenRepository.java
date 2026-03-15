package com.storix.storix_api.domains.user.repository;

import com.storix.storix_api.domains.user.domain.OnboardingToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OnboardingTokenRepository extends CrudRepository<OnboardingToken, String> {
    Optional<OnboardingToken> findByOnboardingToken(String onboardingToken);
}