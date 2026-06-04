package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.domain.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    // 해당 종류의 최신 버전(시행일 기준) 약관 조회
    Optional<Terms> findFirstByTermsTypeOrderByEffectiveFromDesc(TermsType termsType);

    // 같은 종류 + 버전 중복 여부
    boolean existsByTermsTypeAndVersion(TermsType termsType, String version);
}
