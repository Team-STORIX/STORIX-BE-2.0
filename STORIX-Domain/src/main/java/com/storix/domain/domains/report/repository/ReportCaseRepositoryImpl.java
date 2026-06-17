package com.storix.domain.domains.report.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.storix.domain.domains.report.domain.ReportCase;
import com.storix.domain.domains.report.dto.AdminReportSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.storix.domain.domains.report.domain.QReportCase.reportCase;

@RequiredArgsConstructor
public class ReportCaseRepositoryImpl implements ReportCaseRepositoryCustom {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "createdAt", "processedAt", "status", "targetType"
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReportCase> searchReportCases(AdminReportSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = buildCondition(condition);

        List<ReportCase> results = queryFactory
                .selectFrom(reportCase)
                .where(builder)
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(reportCase.count())
                .from(reportCase)
                .where(builder)
                .fetchOne();

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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        if (sort.isUnsorted()) {
            return new OrderSpecifier[]{
                    new OrderSpecifier<>(Order.DESC, reportCase.createdAt)
            };
        }

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        PathBuilder<ReportCase> entityPath = new PathBuilder<>(ReportCase.class, "reportCase");

        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                continue; // 허용되지 않은 필드는 무시 → 기본 정렬로 폴백
            }
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orderSpecifiers.add(new OrderSpecifier(direction, entityPath.get(order.getProperty())));
        }

        if (orderSpecifiers.isEmpty()) {
            return new OrderSpecifier[]{new OrderSpecifier<>(Order.DESC, reportCase.createdAt)};
        }
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}
