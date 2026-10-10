package com.storix.domain.domains.works.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.storix.domain.domains.event.dto.StoryCardLuckyWorkPick;
import com.storix.domain.domains.works.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.storix.domain.domains.works.domain.QWorks.works;
import static com.storix.domain.domains.works.domain.QWorksPlatform.worksPlatform;

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
    public Slice<Works> searchByHashtagWithFilters(
            String hashtagKeyword,
            List<WorksType> worksTypes,
            List<Genre> genres,
            Pageable pageable
    ) {
        if (hashtagKeyword == null || hashtagKeyword.isBlank()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        BooleanBuilder builder = buildFilterCondition(worksTypes, genres);

        // 해시태그명 접두 검색 (#~로 시작하는 해시태그를 가진 작품)
        builder.and(works.hashtags.any().name.startsWith(hashtagKeyword));

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


    @Override
    public Slice<Works> findByIdsWithFilters(
            List<Long> worksIds,
            List<WorksType> worksTypes,
            List<Genre> genres,
            Pageable pageable
    ) {
        if (worksIds.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        BooleanBuilder builder = buildFilterCondition(worksTypes, genres);

        // 기본순은 ES 관련도 순서 그대로
        if (pageable.getSort().isUnsorted()) {
            int from = (int) Math.min(pageable.getOffset(), worksIds.size());
            List<Long> pageIds = worksIds.subList(from, Math.min(from + pageable.getPageSize() + 1, worksIds.size()));
            if (pageIds.isEmpty()) {
                return new SliceImpl<>(List.of(), pageable, false);
            }

            Map<Long, Works> byId = queryFactory
                    .selectFrom(works)
                    .where(builder.and(works.id.in(pageIds)))
                    .fetch().stream()
                    .collect(Collectors.toMap(Works::getId, Function.identity()));
            List<Works> ordered = pageIds.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());

            boolean hasNext = pageIds.size() > pageable.getPageSize();
            if (hasNext && ordered.size() > pageable.getPageSize()) {
                ordered.remove(ordered.size() - 1);
            }
            return new SliceImpl<>(ordered, pageable, hasNext);
        }

        builder.and(works.id.in(worksIds));

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
    public List<Long> findCandidateIds(List<Long> excludedIds, boolean excludeAdult) {
        BooleanBuilder builder = new BooleanBuilder();

        if (excludedIds != null && !excludedIds.isEmpty()) {
            builder.and(works.id.notIn(excludedIds));
        }

        if (excludeAdult) {
            builder.and(works.ageClassification.ne(AgeClassification.AGE_18));
        }

        return queryFactory
                .select(works.id)
                .from(works)
                .where(builder)
                .fetch();
    }

    @Override
    public List<StoryCardLuckyWorkPick> findStoryCardLuckyWorks(Genre genre, boolean excludeAdult) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(works.isStoryCardLuckyWork.isTrue());
        builder.and(works.genre.eq(genre));

        if (excludeAdult) {
            builder.and(works.ageClassification.ne(AgeClassification.AGE_18));
        }

        return queryFactory
                .select(Projections.constructor(StoryCardLuckyWorkPick.class,
                        works.id, works.worksName, works.worksType,
                        worksPlatform.platform, worksPlatform.landingUrl))
                .from(works)
                .join(worksPlatform).on(worksPlatform.works.eq(works))
                .where(builder)
                .orderBy(works.id.asc())
                .fetch();
    }

    // 작품 다중 필터링 공통 로직. 성인 작품도 노출하되 isAdultOnly 플래그로 프론트에서 판단한다
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

        return builder;
    }

    @SuppressWarnings("unchecked")
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        // 기본순인데 ES 점수가 없는 경로는 작품명순
        if (sort.isUnsorted()) {
            return new OrderSpecifier<?>[]{works.worksName.asc(), works.id.asc()};
        }

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        PathBuilder<Works> entityPath = new PathBuilder<>(Works.class, "works");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orderSpecifiers.add(new OrderSpecifier(direction, entityPath.get(order.getProperty())));
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}
