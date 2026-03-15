package com.storix.domain.domains.works.application.port;

import com.storix.domain.domains.works.dto.SlicedWorksInfo;
import com.storix.domain.domains.works.dto.TopicRoomWorksInfo;
import com.storix.domain.domains.works.dto.WorksInfo;
import com.storix.domain.domains.works.dto.LibraryWorksInfo;
import com.storix.domain.domains.works.domain.Works;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;

public interface LoadWorksPort {

    Slice<Works> searchWorks(String keyword, Pageable pageable);
  
    Works findById(Long worksId);

    // 키워드로 작품 ID 리스트만 조회 (검색용)
    List<Long> findAllIdsByKeyword(String keyword);

    Works findByIdWithHashtags(Long worksId);

    // 리뷰 도메인 용
    void checkWorksExistById(Long worksId);

    Boolean isWorksForAdult(Long worksId);

    void updateIncrementingReviewInfoToWorks(Long worksId, double newRating);

    void updateDecrementingReviewInfoToWorks(Long worksId, double newRating);

    // 서재 도메인 용
    List<LibraryWorksInfo> getLibraryWorksInfo(List<Long> worksIds);

    Slice<LibraryWorksInfo> searchLibraryWorksInfoByIds(List<Long> worksIds, String keyword, Pageable pageable);

    Map<Long, WorksInfo> findAllWorksInfoByWorksIds(List<Long> worksIds);

    // 작품 상세 리뷰 용
    WorksInfo findWorksInfoById(Long worksId);

    // 피드 관심 작품 리스트 용
    Map<Long, SlicedWorksInfo> findAllSlicedWorksInfoByWorksIds(List<Long> worksIds);

    Map<Long, TopicRoomWorksInfo> loadWorksMapByIds(List<Long> worksIds);

    // 랜덤 작품 조회 (없으면 빈 리스트)
    List<Works> findRandomWorksExcluding(List<Long> excludedIds, int limit);

    List<Works> findWorksByIds(List<Long> worksIds);
}
