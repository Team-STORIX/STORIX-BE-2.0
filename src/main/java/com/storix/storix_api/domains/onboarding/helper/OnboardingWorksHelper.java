package com.storix.storix_api.domains.onboarding.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.storix_api.domains.onboarding.dto.OnboardingWorksInfo;
import com.storix.storix_api.domains.onboarding.dto.StandardOnboardingWorksInfo;
import com.storix.storix_api.domains.works.application.helper.ArtistNameParseHelper;
import com.storix.storix_api.domains.works.repository.WorksRepository;

import com.storix.storix_api.global.apiPayload.exception.user.InvalidOnboardingWorksException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnboardingWorksHelper {

    private static final String KEY = "onboarding::onboardingWorksList::v1";
    private static final Duration TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private final WorksRepository worksRepository;
    private final ArtistNameParseHelper artistNameParseHelper;

    @Transactional(readOnly = true)
    public List<StandardOnboardingWorksInfo> findOnboardingWorksList() {

        // 1) 캐시 조회
        Object cached = redisTemplate.opsForValue().get(KEY);
        if (cached != null) {
            try {
                String json = (String) cached;

                return objectMapper.readValue(
                        json,
                        new TypeReference<>() {}
                );
            } catch (Exception e) {
                redisTemplate.delete(KEY);
            }
        }

        // 2) DB 조회
        log.info(">>>> [Helper] OnboardingWorksList Cache Update");
        List<OnboardingWorksInfo> raws = worksRepository.findAllOnboardingWorksInfo();

        List<StandardOnboardingWorksInfo> result = raws.stream()
                .map(raw ->
                        StandardOnboardingWorksInfo.of
                                (raw, artistNameParseHelper
                                        .buildArtistName(raw.getOriginalAuthor(), raw.getAuthor(), raw.getIllustrator())
                                )
                )
                .toList();

        // 3) 캐시 저장
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(KEY, json, TTL);
        } catch (Exception e) {
            log.warn(">>>> [Helper] OnboardingWorksList Cache Write Failed. key={}", KEY, e);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public void checkReaderSignUpWithOnboardingWorksList(Set<Long> worksIds) {

        if (worksIds == null || worksIds.isEmpty()) {
            throw InvalidOnboardingWorksException.EXCEPTION;
        }

        List<StandardOnboardingWorksInfo> cachedList = findOnboardingWorksList();

        Set<Long> validWorksIds = cachedList.stream()
                .map(StandardOnboardingWorksInfo::getWorksId)
                .collect(java.util.stream.Collectors.toSet());

        for (Long id : worksIds) {
            if (!validWorksIds.contains(id)) {
                throw InvalidOnboardingWorksException.EXCEPTION;
            }
        }
    }

}
