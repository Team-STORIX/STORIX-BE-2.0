package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.domain.TermsType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    // 해당 종류의 현재 시행 중인 약관(시행일 <= 오늘 <= 종료일(또는 무기한), 시행일 기준 최신) 조회
    @Query("""
            SELECT t FROM Terms t
            WHERE t.termsType = :termsType
              AND t.effectiveFrom <= :today
              AND (t.effectiveTo IS NULL OR t.effectiveTo >= :today)
            ORDER BY t.effectiveFrom DESC
    """)
    List<Terms> findCurrentlyEffective(@Param("termsType") TermsType termsType,
                                       @Param("today") LocalDate today,
                                       Pageable pageable);

    // 같은 종류 + 버전 중복 여부
    boolean existsByTermsTypeAndVersion(TermsType termsType, String version);
}
