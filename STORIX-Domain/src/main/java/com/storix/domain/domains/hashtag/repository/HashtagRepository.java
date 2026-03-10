package com.storix.domain.domains.hashtag.repository;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.hashtag.dto.HashtagInfo;
import com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto;
import com.storix.domain.domains.works.domain.Genre;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    @Query("SELECT new com.storix.domain.domains.hashtag.dto.HashtagInfo(w.id, h.name) " +
            "FROM Hashtag  h " +
            "JOIN h.works w "+
            "WHERE w.id IN :worksIds " +
            "ORDER BY w.id, h.name")
    List<HashtagInfo> findAllByWorksIds(@Param("worksIds") List<Long> worksIds);

    // 특정 장르들의 작품에서 가장 많이 사용된 해시태그 조회
    @Query("SELECT new com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto(h.id, h.name, COUNT(w)) " +
            "FROM Hashtag h " +
            "JOIN h.works w " +
            "WHERE w.genre IN :genres " +
            "GROUP BY h.id, h.name " +
            "ORDER BY COUNT(w) DESC, h.name ASC")
    List<HashtagRecommendResponseDto> findPopularByGenres(@Param("genres") Set<Genre> genres, Pageable pageable);

    // 전체 작품에서 가장 많이 사용된 해시태그 조회 - 비로그인 시
    @Query("SELECT new com.storix.domain.domains.hashtag.dto.HashtagRecommendResponseDto(h.id, h.name, COUNT(w)) " +
            "FROM Hashtag h " +
            "JOIN h.works w " +
            "GROUP BY h.id, h.name " +
            "ORDER BY COUNT(w) DESC, h.name ASC")
    List<HashtagRecommendResponseDto> findGlobalPopular(Pageable pageable);
}
