package com.storix.domain.domains.works.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.storix.domain.domains.works.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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
            WorksSortType sortType,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 검색
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                    works.worksName.contains(keyword)
                            .or(works.author.contains(keyword))
                            .or(works.illustrator.contains(keyword))
                            .or(works.originalAuthor.contains(keyword))
            );
        }

        // 작품 유형 필터 (다중 선택)
        if (worksTypes != null && !worksTypes.isEmpty()) {
            builder.and(works.worksType.in(worksTypes));
        }

        // 장르 필터 (다중 선택)
        if (genres != null && !genres.isEmpty()) {
            builder.and(works.genre.in(genres));
        }

        // 성인 제외
        builder.and(works.ageClassification.ne(AgeClassification.AGE_18));

        List<Works> results = queryFactory
                .selectFrom(works)
                .where(builder)
                .orderBy(getOrderSpecifier(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results.remove(results.size() - 1);
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    private OrderSpecifier<?>[] getOrderSpecifier(WorksSortType sortType) {
        if (sortType == null) {
            sortType = WorksSortType.NAME;
        }

        return switch (sortType) {
            case RATING -> new OrderSpecifier[]{
                    works.avgRating.desc(),
                    works.id.desc()
            };
            case REVIEW -> new OrderSpecifier[]{
                    works.reviewsCount.desc(),
                    works.id.desc()
            };
            default -> new OrderSpecifier[]{
                    works.worksName.asc()
            };
        };
    }
}
