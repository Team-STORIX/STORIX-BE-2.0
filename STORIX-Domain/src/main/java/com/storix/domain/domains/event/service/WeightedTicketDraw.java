package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.dto.AttendanceTicketHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

// 응모권 수를 가중치로 하는 비복원 추첨.
// 남은 응모권 총합 구간에서 난수를 뽑아 당첨자를 정하고, 그 당첨자의 응모권을 모수에서 제거한 뒤 다음 순위를 뽑는다.
// → 응모권이 많을수록 당첨 확률이 높고, 한 유저가 두 번 당첨되지 않는다.
public final class WeightedTicketDraw {

    private WeightedTicketDraw() {
    }

    // 앞에서부터 1위, 2위 … 순으로 담긴 당첨자 목록.
    // 후보가 요청한 인원보다 적으면 있는 만큼만 반환한다.
    public static List<AttendanceTicketHolder> draw(List<AttendanceTicketHolder> candidates,
                                                    int winnerCount,
                                                    RandomGenerator random) {
        if (candidates == null || candidates.isEmpty() || winnerCount <= 0) {
            return Collections.emptyList();
        }

        List<AttendanceTicketHolder> remaining = new ArrayList<>(candidates);
        long remainingTickets = remaining.stream()
                .mapToLong(AttendanceTicketHolder::ticketCount)
                .sum();

        int drawCount = Math.min(winnerCount, remaining.size());
        List<AttendanceTicketHolder> winners = new ArrayList<>(drawCount);
        for (int rank = 0; rank < drawCount; rank++) {
            AttendanceTicketHolder winner = remaining.remove(pickIndex(remaining, remainingTickets, random));
            remainingTickets -= winner.ticketCount();
            winners.add(winner);
        }
        return List.copyOf(winners);
    }

    // [0, remainingTickets) 난수가 가리키는 응모권의 소유자 인덱스
    private static int pickIndex(List<AttendanceTicketHolder> remaining, long remainingTickets, RandomGenerator random) {
        long target = random.nextLong(remainingTickets);
        long cursor = 0;
        for (int i = 0; i < remaining.size(); i++) {
            cursor += remaining.get(i).ticketCount();
            if (target < cursor) {
                return i;
            }
        }
        // 응모권 총합과 후보 목록이 어긋나지 않는 한 도달하지 않는다
        return remaining.size() - 1;
    }
}
