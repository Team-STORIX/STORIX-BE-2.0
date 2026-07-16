package com.storix.domain.domains.report.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.storix.domain.domains.report.domain.QReportCase.reportCase;
import static com.storix.domain.domains.user.domain.QUser.user;

@RequiredArgsConstructor
public class ReportCaseRepositoryImpl implements ReportCaseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReportCase> searchReportCases(AdminReportSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = buildCondition(condition);

        // 피신고자 닉네임 검색 시 User 조인
        boolean joinUser = condition != null && StringUtils.hasText(condition.reportedUserKeyword());
        if (joinUser) {
            builder.and(user.nickName.containsIgnoreCase(condition.reportedUserKeyword().trim()));
        }

        JPAQuery<ReportCase> contentQuery = queryFactory.selectFrom(reportCase);
        JPAQuery<Long> countQuery = queryFactory.select(reportCase.count()).from(reportCase);
        if (joinUser) {
            contentQuery.join(user).on(user.id.eq(reportCase.reportedUserId));
            countQuery.join(user).on(user.id.eq(reportCase.reportedUserId));
        }

        List<ReportCase> results = contentQuery
                .where(builder)
                .orderBy(reportCase.id.desc()) // 접수 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = countQuery.where(builder).fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0);
    }

    private BooleanBuilder buildCondition(AdminReportSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition == null) {
            return builder;
        }

        if (condition.targetType() != null) {
            builder.and(reportCase.targetType.eq(condition.targetType()));
        }

        if (condition.status() != null) {
            builder.and(reportCase.status.eq(condition.status()));
        }

        if (condition.startAt() != null) {
            builder.and(reportCase.createdAt.goe(condition.startAt()));
        }

        if (condition.endAt() != null) {
            builder.and(reportCase.createdAt.loe(condition.endAt()));
        }

        if (condition.reportedUserId() != null) {
            builder.and(reportCase.reportedUserId.eq(condition.reportedUserId()));
        }

        return builder;
    }
}
