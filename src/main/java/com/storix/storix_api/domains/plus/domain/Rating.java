package com.storix.storix_api.domains.plus.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.storix.storix_api.global.apiPayload.exception.plus.InvalidRatingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Rating {

    ZERO_POINT_FIVE("0.5", 0.5),
    ONE("1.0", 1.0),
    ONE_POINT_FIVE("1.5", 1.5),
    TWO("2.0", 2.0),
    TWO_POINT_FIVE("2.5", 2.5),
    THREE("3.0", 3.0),
    THREE_POINT_FIVE("3.5", 3.5),
    FOUR("4.0", 4.0),
    FOUR_POINT_FIVE("4.5", 4.5),
    FIVE("5.0", 5.0);

    private final String dbValue;
    private final double ratingValue;

    // Map { String / BigDecimal }
    private static final Map<String, Rating> BY_DB_STRING =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(
                            Rating::getDbValue,
                            Function.identity()
                    ));

    private static final Map<BigDecimal, Rating> BY_DB_DECIMAL =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(
                            r -> new BigDecimal(r.dbValue),
                            Function.identity()
                    ));

    // 응답 DTO용
    @JsonCreator
    public static Rating from(String value) {
        if (value == null) return null;
        Rating rating = BY_DB_STRING.get(value);
        if (rating == null) throw InvalidRatingException.EXCEPTION;
        return rating;
    }

    @JsonValue
    public String toJson() {
        return dbValue;
    }

    // DB BigDecimal -> Rating 변환용
    public static Rating from(BigDecimal value) {
        if (value == null) return null;

        BigDecimal key = value.stripTrailingZeros().setScale(1, RoundingMode.UNNECESSARY);

        Rating rating = BY_DB_DECIMAL.get(key);
        if (rating == null) throw InvalidRatingException.EXCEPTION;
        return rating;
    }

}