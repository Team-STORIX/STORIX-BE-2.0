package com.storix.domain.domains.plus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ReaderBoardInfo(
        // 유저 정보
        Long userId,

        // 게시글 정보
        Long boardId,
        Boolean isWorksSelected,
        Long worksId,
        String lastCreatedTime,
        String content,
        int likeCount,
        int replyCount,
        boolean isSpoiler,

        // 좋아요 여부
        boolean isLiked
) {
    // 내 게시글 조회
    public static ReaderBoardInfo ofMyBoard(ReaderBoard board, boolean isLiked) {
        return ReaderBoardInfo.builder()
                .userId(null)
                .boardId(board.getId())
                .isWorksSelected(board.isWorksSelected())
                .worksId(board.getWorksId())
                .lastCreatedTime(formatTimeAgo(board.getCreatedAt()))
                .content(board.getContent())
                .likeCount(board.getLikeCount())
                .replyCount(board.getReplyCount())
                .isSpoiler(board.isSpoiler())
                .isLiked(isLiked)
                .build();
    }

    // 피드 게시글 조회
    public static ReaderBoardInfo ofFeedBoard(ReaderBoard board, boolean isLiked) {
        return ReaderBoardInfo.builder()
                .userId(board.getUserId())
                .boardId(board.getId())
                .isWorksSelected(board.isWorksSelected())
                .worksId(board.getWorksId())
                .lastCreatedTime(formatTimeAgo(board.getCreatedAt()))
                .content(board.getContent())
                .likeCount(board.getLikeCount())
                .replyCount(board.getReplyCount())
                .isSpoiler(board.isSpoiler())
                .isLiked(isLiked)
                .build();
    }

    // 오늘의 피드 게시글 조회
    public static ReaderBoardInfo ofHomeBoard(StandardReaderBoardInfo board, boolean isLiked) {
        return ReaderBoardInfo.builder()
                .userId(board.userId())
                .boardId(board.boardId())
                .isWorksSelected(null)
                .worksId(null)
                .lastCreatedTime(null)
                .content(board.content())
                .likeCount(board.likeCount())
                .replyCount(board.replyCount())
                .isSpoiler(board.isSpoiler())
                .isLiked(isLiked)
                .build();
    }


    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private static String formatTimeAgo(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(time, now).toMinutes();

        if (minutes < 1) { return "방금 전"; }
        if (minutes < 60) { return minutes + "분 전"; }

        long hours = minutes / 60;
        if (hours < 24) { return hours + "시간 전"; }

        long days = hours / 24;
        if (days < 7) { return days + "일 전"; }

        return time.format(DATE_TIME_FORMATTER);
    }
}