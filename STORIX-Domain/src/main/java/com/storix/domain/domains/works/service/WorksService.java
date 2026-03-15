package com.storix.domain.domains.works.service;

import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.user.application.port.LoadUserPort;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.application.usecase.WorksUseCase;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.WorksDetailResponseDto;
import com.storix.domain.domains.topicroom.exception.UnverifiedException;
import com.storix.domain.domains.user.exception.auth.LoginRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorksService implements WorksUseCase {

    private final LoadWorksPort loadWorksPort;
    private final LoadUserPort loadUserPort;

    private final ReviewAdaptor reviewAdaptor;

    @Override
    public WorksDetailResponseDto getWorksDetail(Long userId, Long worksId) {

        Works works = loadWorksPort.findByIdWithHashtags(worksId);

        // 18세 이용가 작품인지 확인
        if ("18세 이용가".equals(works.getAgeClassification().getDbValue())) {

            // 비로그인 유저
            if (userId == null) {
                throw LoginRequiredException.EXCEPTION;
            }

            // 로그인 유저지만 성인 인증 되지 않은 경우
            Boolean isAdult = loadUserPort.findIsAdultVerifiedById(userId);

            if (!Boolean.TRUE.equals(isAdult)) {
                throw UnverifiedException.EXCEPTION;
            }
        }

        long reviewCount = reviewAdaptor.getReviewCount(worksId);

        return WorksDetailResponseDto.from(works, reviewCount);
    }


}
