package com.storix.domain.domains.works.service;

import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.user.application.port.LoadUserPort;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.application.usecase.WorksUseCase;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import com.storix.domain.domains.works.dto.WorksDetailResponseDto;
import com.storix.domain.domains.topicroom.exception.UnverifiedException;
import com.storix.domain.domains.user.exception.auth.LoginRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorksService implements WorksUseCase {

    private final LoadWorksPort loadWorksPort;
    private final LoadUserPort loadUserPort;
    private final LoadTopicRoomPort loadTopicRoomPort;

    private final ReviewAdaptor reviewAdaptor;
    private final WorksAdaptor worksAdaptor;


    @Override
    @Transactional(readOnly = true)
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
        boolean hasTopicRoom = loadTopicRoomPort.existsByWorksId(worksId);

        return WorksDetailResponseDto.from(works, reviewCount, hasTopicRoom);
    }

    @Transactional(readOnly = true)
    public Map<Long, TopicRoomWorksInfo> getWorksMapByIds(List<Long> worksIds){
        return worksAdaptor.loadWorksMapByIds(worksIds);
    }

    @Transactional(readOnly = true)
    public List<Long> getIdsByKeyword(String keyword){
        return worksAdaptor.findAllIdsByKeyword(keyword);
    }

    @Transactional(readOnly = true)
    public Works findWorksById(Long worksId) {
        return worksAdaptor.findById(worksId);
    }

}
