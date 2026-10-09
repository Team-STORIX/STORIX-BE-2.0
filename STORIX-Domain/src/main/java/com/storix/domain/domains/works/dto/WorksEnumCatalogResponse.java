package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.AgeClassification;
import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public record WorksEnumCatalogResponse(
        EnumBlock genre,
        EnumBlock ageClassification,
        EnumBlock worksType,
        EnumBlock platform
) {

    private static final String STORED_AS_DB_VALUE = "dbValue";
    private static final String STORED_AS_NAME = "name";

    // 플랫폼만 @Enumerated(STRING) 이라 DB 에 name 이 들어가고, 나머지는 컨버터가 dbValue 로 바꿔 넣는다
    public static WorksEnumCatalogResponse create() {
        return new WorksEnumCatalogResponse(
                EnumBlock.of(STORED_AS_DB_VALUE, Genre.values(), Genre::getDbValue),
                EnumBlock.of(STORED_AS_DB_VALUE, AgeClassification.values(), AgeClassification::getDbValue),
                EnumBlock.of(STORED_AS_DB_VALUE, WorksType.values(), WorksType::getDbValue),
                EnumBlock.of(STORED_AS_NAME, Platform.values(), Platform::getDbValue)
        );
    }

    public record EnumBlock(String storedAs, List<EnumItem> values) {

        private static <E extends Enum<E>> EnumBlock of(String storedAs, E[] values, Function<E, String> dbValue) {
            return new EnumBlock(storedAs, Arrays.stream(values)
                    .map(value -> new EnumItem(value.name(), dbValue.apply(value)))
                    .toList());
        }
    }

    public record EnumItem(String name, String dbValue) {
    }
}
