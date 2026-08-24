package com.storix.domain.domains.works.service;

import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.application.usecase.WorksUseCase;
import com.storix.domain.domains.works.domain.AdultContentPolicy;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.WorksDetailResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorksService implements WorksUseCase {

    private final LoadWorksPort loadWorksPort;
    private final AdultVerificationAdaptor adultVerificationAdaptor;
    private final LoadTopicRoomPort loadTopicRoomPort;

    private final ReviewAdaptor reviewAdaptor;

    @Override
    @Transactional(readOnly = true)
    public WorksDetailResponseDto getWorksDetail(Long userId, Long worksId) {

        Works works = loadWorksPort.findByIdWithHashtags(worksId);

        if (works.getWorksType() == null || works.getGenre() == null || works.getAgeClassification() == null) {
            log.atWarn()
                    .addKeyValue("worksId", worksId)
                    .addKeyValue("worksTypeMissing", works.getWorksType() == null)
                    .addKeyValue("genreMissing", works.getGenre() == null)
                    .addKeyValue("ageClassificationMissing", works.getAgeClassification() == null)
                    .log(">>> [Works] enum 컬럼 값 없음");
        }

        // 18세 이용가 작품인지 확인
        AdultContentPolicy.check(
                works.getAgeClassification(),
                () -> adultVerificationAdaptor.findLatestVerifiedAtByUserId(userId)
        );

        long reviewCount = reviewAdaptor.getReviewCount(worksId);
        boolean hasTopicRoom = loadTopicRoomPort.existsByWorksId(worksId);

        return WorksDetailResponseDto.from(works, reviewCount, hasTopicRoom);
    }
}
