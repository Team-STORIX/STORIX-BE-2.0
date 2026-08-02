package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.dto.AttendanceTicketHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[출석 이벤트] 응모권 가중치 추첨")
class WeightedTicketDrawTest {

    private static AttendanceTicketHolder holder(long userId, int tickets) {
        return new AttendanceTicketHolder(userId, tickets, tickets);
    }

    // 지정한 값을 순서대로 돌려주는 난수원 (nextLong(bound) 호출만 사용)
    private static RandomGenerator fixed(long... values) {
        return new RandomGenerator() {
            private int index = 0;

            @Override
            public long nextLong() {
                return values[index++];
            }

            @Override
            public long nextLong(long bound) {
                return nextLong() % bound;
            }
        };
    }

    @Test
    @DisplayName("난수가 가리키는 응모권의 소유자가 당첨되고, 뽑힌 순서대로 1위·2위가 정해진다")
    void draws_in_ticket_order() {
        // 응모권 구간: A[0,1) B[1,4) C[4,9)
        List<AttendanceTicketHolder> candidates = List.of(holder(1L, 1), holder(2L, 3), holder(3L, 5));

        // 1회차: 5 → C 당첨 / 2회차: 남은 4장 중 1 → B 당첨
        List<AttendanceTicketHolder> winners = WeightedTicketDraw.draw(candidates, 2, fixed(5, 1));

        assertThat(winners).extracting(AttendanceTicketHolder::userId).containsExactly(3L, 2L);
    }

    @Test
    @DisplayName("한 번 당첨된 유저는 다음 순위 추첨 모수에서 빠진다")
    void does_not_draw_same_user_twice() {
        List<AttendanceTicketHolder> candidates = List.of(holder(1L, 1), holder(2L, 99));

        // 매번 0을 뽑아도(=구간 맨 앞) 같은 유저가 중복 당첨되지 않는다
        List<AttendanceTicketHolder> winners = WeightedTicketDraw.draw(candidates, 2, fixed(0, 0));

        assertThat(winners).extracting(AttendanceTicketHolder::userId).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("요청 인원이 후보 수보다 많으면 있는 만큼만 반환한다")
    void caps_at_candidate_count() {
        List<AttendanceTicketHolder> candidates = List.of(holder(1L, 2), holder(2L, 2));

        List<AttendanceTicketHolder> winners = WeightedTicketDraw.draw(candidates, 10, fixed(0, 0));

        assertThat(winners).hasSize(2);
    }

    @Test
    @DisplayName("후보가 없거나 인원이 0 이하면 빈 목록을 반환한다")
    void empty_cases() {
        assertThat(WeightedTicketDraw.draw(List.of(), 3, fixed(0))).isEmpty();
        assertThat(WeightedTicketDraw.draw(List.of(holder(1L, 1)), 0, fixed(0))).isEmpty();
    }

    @Test
    @DisplayName("응모권이 많을수록 당첨 확률이 높아진다")
    void more_tickets_means_higher_win_rate() {
        List<AttendanceTicketHolder> candidates = List.of(holder(1L, 1), holder(2L, 10));
        RandomGenerator random = new Random(42);

        Map<Long, Long> winCounts = IntStream.range(0, 10_000)
                .mapToObj(i -> WeightedTicketDraw.draw(candidates, 1, random).get(0).userId())
                .collect(Collectors.groupingBy(userId -> userId, Collectors.counting()));

        // 기대 비율 1:10 — 시드 고정이므로 결과가 흔들리지 않는다
        assertThat(winCounts.get(2L)).isGreaterThan(winCounts.get(1L) * 5);
    }
}
