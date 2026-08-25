package com.storix.domain.domains.works.application.helper;

import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.exception.UnknownWorksException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdultWorksHelper {

    private final WorksAdaptor worksAdaptor;
    private final AdultVerificationAdaptor adultVerificationAdaptor;

    // 성인 작품 여부 및 유저 성인 인증 여부 확인
    public void CheckUserAuthorityWithWorks(Long userId, Long worksId) {

        Boolean isWorksForAdult = worksAdaptor.isWorksForAdult(worksId);

        // DB에 없는 작품일 경우
        if (isWorksForAdult == null) {
            throw UnknownWorksException.EXCEPTION;
        }

        AdultContentPolicy.check(
                isWorksForAdult,
                () -> adultVerificationAdaptor.findLatestVerifiedAtByUserId(userId)
        );
    }

}
