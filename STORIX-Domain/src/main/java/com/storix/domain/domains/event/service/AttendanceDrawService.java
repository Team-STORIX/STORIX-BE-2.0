package com.storix.domain.domains.event.service;

import com.storix.domain.domains.event.adaptor.AppEventAdaptor;
import com.storix.domain.domains.event.adaptor.AttendanceCheckAdaptor;
import com.storix.domain.domains.event.domain.AppEvent;
import com.storix.domain.domains.event.domain.AppEventType;
import com.storix.domain.domains.event.dto.AttendanceDrawResponse;
import com.storix.domain.domains.event.dto.AttendanceDrawWinner;
import com.storix.domain.domains.event.dto.AttendanceTicketHolder;
import com.storix.domain.domains.event.exception.AttendanceDrawInvalidWinnerCountException;
import com.storix.domain.domains.event.exception.AttendanceEventNotFoundException;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.AdminUserContactInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AttendanceDrawService {

    // 추첨 결과가 예측 가능하면 안 되므로 SecureRandom 사용
    private static final RandomGenerator RANDOM = new SecureRandom();

    private final AppEventAdaptor appEventAdaptor;
    private final AttendanceCheckAdaptor attendanceCheckAdaptor;
    private final UserAdaptor userAdaptor;

    // 응모권 수를 가중치로 당첨자를 뽑아 1위부터 순위순으로 반환한다.
    // 결과를 저장하지 않으므로 호출할 때마다 새로 추첨된다.
    @Transactional(readOnly = true)
    public AttendanceDrawResponse draw(Long appEventId, int winnerCount) {
        if (winnerCount <= 0) {
            throw AttendanceDrawInvalidWinnerCountException.EXCEPTION;
        }
        AppEvent event = resolveEvent(appEventId);

        List<AttendanceTicketHolder> candidates = ticketHoldersOf(event);
        List<AttendanceTicketHolder> drawn = WeightedTicketDraw.draw(candidates, winnerCount, RANDOM);

        Map<Long, AdminUserContactInfo> userInfos = userAdaptor.findAdminUserContactInfoByUserIds(
                drawn.stream().map(AttendanceTicketHolder::userId).toList()
        );

        return AttendanceDrawResponse.builder()
                .appEventId(event.getId())
                .requestedWinnerCount(winnerCount)
                .candidateCount(candidates.size())
                .totalTickets(candidates.stream().mapToInt(AttendanceTicketHolder::ticketCount).sum())
                .winners(toWinners(drawn, userInfos))
                .build();
    }

    // 관리자가 지정한 이벤트로 추첨한다. 종료된 이벤트도 대상이므로 진행 여부는 보지 않지만,
    // 출석 이벤트가 아니면 응모권 개념이 없으므로 404로 막는다
    private AppEvent resolveEvent(Long appEventId) {
        if (appEventId == null || appEventId <= 0) {
            throw AttendanceEventNotFoundException.EXCEPTION;
        }
        return appEventAdaptor.findOptionalById(appEventId)
                .filter(event -> event.getEventType() == AppEventType.ATTENDANCE)
                .orElseThrow(() -> AttendanceEventNotFoundException.EXCEPTION);
    }

    // 참여자별 누적 출석일을 이벤트 지급표에 태워 응모권 수로 환산한다. 응모권이 없으면 추첨 모수에서 빠진다.
    private List<AttendanceTicketHolder> ticketHoldersOf(AppEvent event) {
        NavigableMap<Integer, Integer> schedule = AttendanceTicketPolicy.scheduleOf(event);
        return attendanceCheckAdaptor.findAttendeeCounts(event.getId()).stream()
                .map(attendee -> {
                    int attendedDays = Math.toIntExact(attendee.attendedDays());
                    return new AttendanceTicketHolder(
                            attendee.userId(),
                            attendedDays,
                            AttendanceTicketPolicy.issuedTicketsFor(schedule, attendedDays)
                    );
                })
                .filter(holder -> holder.ticketCount() > 0)
                .toList();
    }

    private static List<AttendanceDrawWinner> toWinners(List<AttendanceTicketHolder> drawn,
                                                        Map<Long, AdminUserContactInfo> userInfos) {
        return IntStream.range(0, drawn.size())
                .mapToObj(index -> {
                    AttendanceTicketHolder holder = drawn.get(index);
                    AdminUserContactInfo info = userInfos.get(holder.userId());
                    return AttendanceDrawWinner.builder()
                            .rank(index + 1)
                            .userId(holder.userId())
                            .nickName(info == null ? null : info.nickName())
                            .email(info == null ? null : info.email())
                            .profileImageUrl(info == null ? null : info.profileImageUrl())
                            .ticketCount(holder.ticketCount())
                            .totalAttendedDays(holder.totalAttendedDays())
                            .build();
                })
                .toList();
    }
}
