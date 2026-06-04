package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.adaptor.TermsAdaptor;
import com.storix.domain.domains.user.domain.Terms;
import com.storix.domain.domains.user.dto.CreateTermsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsAdaptor termsAdaptor;

    // 약관 등록
    @Transactional
    public Terms register(CreateTermsCommand cmd) {
        return termsAdaptor.register(cmd);
    }
}
