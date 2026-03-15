package com.storix.domain.domains.works.application.helper;

import com.storix.domain.domains.user.application.port.LoadUserPort;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.topicroom.exception.UnverifiedException;
import com.storix.domain.domains.user.exception.auth.LoginRequiredException;
import com.storix.domain.domains.works.exception.UnknownWorksException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdultWorksHelper {

    private final LoadWorksPort loadWorksPort;
    private final LoadUserPort loadUserPort;

    // 성인 작품 여부 및 유저 성인 인증 여부 확인
    public void CheckUserAuthorityWithWorks(Long userId, Long worksId) {

        Boolean isWorksForAdult = loadWorksPort.isWorksForAdult(worksId);

        // DB에 없는 작품일 경우
        if (isWorksForAdult == null) {
            throw UnknownWorksException.EXCEPTION;
        }

        // DB에 있는 성인 작품일 경우
        if (isWorksForAdult) {

            // 비로그인 유저
            if (userId == null) {
                throw LoginRequiredException.EXCEPTION;
            }

            // 로그인 유저 성인 인증 여부 확인
            Boolean isAdultVerifiedById = loadUserPort.findIsAdultVerifiedById(userId);
            if (isAdultVerifiedById != true) {
                throw UnverifiedException.EXCEPTION;
            }
        }

    }

}
