package com.storix.domain.domains.plus.repository;

import java.time.LocalDateTime;

public interface ReaderBoardRankingRepository {

    int updatePopularityScoresRecentDays(LocalDateTime threshold);
}
