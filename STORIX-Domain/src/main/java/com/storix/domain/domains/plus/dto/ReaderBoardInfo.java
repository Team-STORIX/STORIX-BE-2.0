package com.storix.domain.domains.plus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.storix.domain.domains.feed.domain.BoardTheme;
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
        String spoilerScript,
        BoardTheme theme,

        // 좋아요 여부
        boolean isLiked,

        // 참조 작품이 성인 작품인지 여부. 작품 미선택이면 false
        Boolean isAdultOnly,
        Boolean isBlinded
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
                .spoilerScript(board.getSpoilerScript())
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
                .spoilerScript(board.getSpoilerScript())
                .isLiked(isLiked)
                .theme(board.getTheme())
                .build();
    }

    // 오늘의 피드 게시글 조회
    public static ReaderBoardInfo ofHomeBoard(StandardReaderBoardInfo board, boolean isLiked, boolean isAdultOnly) {
        return ReaderBoardInfo.builder()
                .userId(board.userId())
                .boardId(board.boardId())
                .isWorksSelected(board.isWorksSelected())
                .worksId(board.worksId())
                .lastCreatedTime(null)
                .content(board.content())
                .likeCount(board.likeCount())
                .replyCount(board.replyCount())
                .isSpoiler(board.isSpoiler())
                .spoilerScript(board.spoilerScript())
                .isLiked(isLiked)
                .isAdultOnly(isAdultOnly)
                .isBlinded(false)
                .build();
    }


    public ReaderBoardInfo withAdultOnly(boolean adultOnly) {
        return ReaderBoardInfo.builder()
                .userId(userId)
                .boardId(boardId)
                .isWorksSelected(isWorksSelected)
                .worksId(worksId)
                .lastCreatedTime(lastCreatedTime)
                .content(content)
                .likeCount(likeCount)
                .replyCount(replyCount)
                .isSpoiler(isSpoiler)
                .spoilerScript(spoilerScript)
                .theme(theme)
                .isLiked(isLiked)
                .isAdultOnly(adultOnly)
                .isBlinded(false)
                .build();
    }

    // 성인 인증이 유효하지 않은 유저용
    // 프로필·좋아요·댓글 수만 남기고 나머지 게시글 정보는 반환하지 않는다
    public static ReaderBoardInfo ofMaskedAdultBoard(ReaderBoardInfo origin) {
        return ReaderBoardInfo.builder()
                .userId(origin.userId())
                .boardId(origin.boardId())
                .likeCount(origin.likeCount())
                .replyCount(origin.replyCount())
                .isLiked(origin.isLiked())
                .isAdultOnly(true)
                .isBlinded(true)
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