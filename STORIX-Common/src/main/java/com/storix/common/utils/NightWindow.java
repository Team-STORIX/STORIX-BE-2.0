package com.storix.common.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;

// 야간 광고성 정보 발송 제한 시간대: 21:00 ~ 익일 08:00 (KST)
public final class NightWindow {

    private static final LocalTime START = LocalTime.of(21, 0);
    private static final LocalTime END = LocalTime.of(8, 0);

    private NightWindow() {}

    public static boolean isNight(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(START) || time.isBefore(END);
    }

    // 가장 가까운 발송 가능 시각(다음 08:00). 주간이면 입력값 그대로
    public static LocalDateTime nextAllowedAt(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        if (!time.isBefore(START)) {
            return dateTime.toLocalDate().plusDays(1).atTime(END);
        }
        if (time.isBefore(END)) {
            return dateTime.toLocalDate().atTime(END);
        }
        return dateTime;
    }
}
