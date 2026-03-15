package com.storix.storix_api.domains.plus.repository;

import java.time.LocalDateTime;

public interface ReaderBoardRankingRepository {

    int updatePopularityScoresRecentDays(LocalDateTime threshold);
}
