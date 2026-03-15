package com.storix.domain.domains.feed.dto;

import com.storix.domain.domains.feed.domain.ReaderBoardReply;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ReaderBoardReplyInfo(
        Long replyId,
        Long userId,
        Long boardId,
        String comment,
        String lastCreatedTime,
        int likeCount
) {
    public static ReaderBoardReplyInfo from(ReaderBoardReply reply) {
        return new ReaderBoardReplyInfo(
                reply.getId(),
                reply.getUserId(),
                reply.getBoardId(),
                reply.getComment(),
                formatTimeAgo(reply.getCreatedAt()),
                reply.getLikeCount()
        );
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
