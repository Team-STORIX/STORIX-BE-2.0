package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.favorite.domain.FavoriteWorks;
import com.storix.domain.domains.genrescore.domain.UserGenreScoreLog;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.domain.Review;
import com.storix.domain.domains.preference.domain.PreferenceExploration;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorksMergeTarget {

    WORKS_PLATFORM(WorksPlatform.class),
    WORKS_NICKNAME(WorksNickname.class),
    FAVORITE_WORKS(FavoriteWorks.class),
    GENRE_SCORE_LOG(UserGenreScoreLog.class),
    TASTE_EXPLORATION(PreferenceExploration.class),
    REVIEW(Review.class),
    READER_BOARD(ReaderBoard.class),
    TOPIC_ROOM(TopicRoom.class);

    private final Class<?> entity;

    public String tableName() {
        return entity.getAnnotation(Table.class).name();
    }
}
