package com.storix.domain.domains.works.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.storix.domain.domains.works.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static com.storix.domain.domains.works.domain.QWorks.works;

@RequiredArgsConstructor
public class WorksRepositoryImpl implements WorksRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Works> searchWithFilters(
            String keyword,
            List<WorksType> worksTypes,
            List<Genre> genres,
            Pageable pageable
    ) {
        BooleanBuilder builder = buildFilterCondition(worksTypes, genres);

        // 작품명 + 작가명 검색
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                    works.worksName.contains(keyword)
                            .or(works.author.contains(keyword))
                            .or(works.illustrator.contains(keyword))
                            .or(works.originalAuthor.contains(keyword))
            );
        }

        List<Works> results = queryFactory
                .selectFrom(works)
                .where(builder)
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results.remove(results.size() - 1);
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    @Override
    public List<Long> searchIdsWithFilters(
            String keyword,
            List<WorksType> worksTypes,
            List<Genre> genres
    ) {
        BooleanBuilder builder = buildFilterCondition(worksTypes, genres);

        // 작품명만 검색
        if (keyword != null && !keyword.isBlank()) {
            builder.and(works.worksName.contains(keyword));
        }

        return queryFactory
                .select(works.id)
                .from(works)
                .where(builder)
                .fetch();
    }


    // 작품 다중 필터링 공통 로직
    private BooleanBuilder buildFilterCondition(List<WorksType> worksTypes, List<Genre> genres) {
        BooleanBuilder builder = new BooleanBuilder();

        // 1. 작품 유형 필터링
        if (worksTypes != null && !worksTypes.isEmpty()) {
            builder.and(works.worksType.in(worksTypes));
        }

        // 2. 장르 필터링
        if (genres != null && !genres.isEmpty()) {
            builder.and(works.genre.in(genres));
        }

        // 3. 성인 작품 제외 (성인 인증 기능 추가 후 제거)
        builder.and(works.ageClassification.ne(AgeClassification.AGE_18));

        return builder;
    }

    @SuppressWarnings("unchecked")
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        PathBuilder<Works> entityPath = new PathBuilder<>(Works.class, "works");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orderSpecifiers.add(new OrderSpecifier(direction, entityPath.get(order.getProperty())));
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}
