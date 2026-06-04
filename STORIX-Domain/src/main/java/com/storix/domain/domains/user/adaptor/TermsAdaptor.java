package com.storix.domain.domains.user.adaptor;

import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.domain.TermsType;
import com.storix.domain.domains.user.dto.CreateTermsCommand;
import com.storix.domain.domains.user.exception.terms.DuplicateTermsVersionException;
import com.storix.domain.domains.user.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TermsAdaptor {

    private final TermsRepository termsRepository;

    // 약관 등록
    public Terms register(CreateTermsCommand cmd) {
        if (termsRepository.existsByTermsTypeAndVersion(cmd.termsType(), cmd.version())) {
            throw DuplicateTermsVersionException.EXCEPTION;
        }
        return termsRepository.save(cmd.toEntity());
    }

    // 해당 종류의 최신 버전 약관 조회
    public Terms findCurrentByType(TermsType termsType) {
        return termsRepository.findFirstByTermsTypeOrderByEffectiveFromDesc(termsType).orElseThrow();
    }
}
