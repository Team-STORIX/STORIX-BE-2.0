package com.storix.domain.domains.works.service;

import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.adultverification.adaptor.AdultVerificationAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
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
public class WorksService {

    private final TopicRoomAdaptor topicRoomAdaptor;
    private final WorksAdaptor worksAdaptor;
    private final AdultVerificationAdaptor adultVerificationAdaptor;

    private final ReviewAdaptor reviewAdaptor;

    @Transactional(readOnly = true)
    public WorksDetailResponseDto getWorksDetail(Long userId, Long worksId) {

        Works works = worksAdaptor.findByIdWithHashtags(worksId);

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
        boolean hasTopicRoom = topicRoomAdaptor.existsByWorksId(worksId);

        return WorksDetailResponseDto.from(works, reviewCount, hasTopicRoom);
    }
}
