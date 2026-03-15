package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.OnboardingToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OnboardingTokenRepository extends CrudRepository<OnboardingToken, String> {
    Optional<OnboardingToken> findByOnboardingToken(String onboardingToken);
}