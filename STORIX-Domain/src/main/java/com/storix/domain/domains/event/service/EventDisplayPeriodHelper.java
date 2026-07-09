package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.ContentTargetType;
import com.storix.domain.domains.event.dto.DisplayPeriod;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;


@Component
public class EventDisplayPeriodHelper {

    public void validate(LocalDateTime displayStartAt, LocalDateTime displayEndAt, Supplier<RuntimeException> onInvalid) {
        if (displayStartAt == null || displayEndAt == null || !displayStartAt.isBefore(displayEndAt)) {
            throw onInvalid.get();
        }
    }

    public void requireAppEventForType(ContentTargetType contentTargetType, boolean hasAppEvent, Supplier<RuntimeException> onMissing) {
        if (contentTargetType == ContentTargetType.APP_EVENT && !hasAppEvent) {
            throw onMissing.get();
        }
    }

    public DisplayPeriod clampToAppEvent(AppEvent appEvent, LocalDateTime displayStartAt, LocalDateTime displayEndAt, Supplier<RuntimeException> onOutOfPeriod) {
        if (appEvent == null) {
            return new DisplayPeriod(displayStartAt, displayEndAt);
        }
        LocalDateTime start = displayStartAt.isBefore(appEvent.getStartAt()) ? appEvent.getStartAt() : displayStartAt;
        LocalDateTime end = displayEndAt.isAfter(appEvent.getEndAt()) ? appEvent.getEndAt() : displayEndAt;
        if (!start.isBefore(end)) {
            throw onOutOfPeriod.get();
        }
        return new DisplayPeriod(start, end);
    }

    public int maxConcurrent(List<DisplayPeriod> periods, LocalDateTime windowStart) {
        int n = periods.size();
        LocalDateTime[] starts = new LocalDateTime[n];
        LocalDateTime[] ends = new LocalDateTime[n];
        for (int i = 0; i < n; i++) {
            DisplayPeriod p = periods.get(i);
            starts[i] = p.start().isBefore(windowStart) ? windowStart : p.start();
            ends[i] = p.end();
        }
        Arrays.sort(starts);
        Arrays.sort(ends);

        int concurrent = 0;
        int peak = 0;
        int end = 0;
        for (LocalDateTime start : starts) {
            while (ends[end].isBefore(start)) {
                concurrent--;
                end++;
            }
            concurrent++;
            peak = Math.max(peak, concurrent);
        }
        return peak;
    }
}
