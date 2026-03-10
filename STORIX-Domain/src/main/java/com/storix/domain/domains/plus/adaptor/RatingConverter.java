package com.storix.domain.domains.plus.adaptor;

import com.storix.domain.domains.plus.domain.Rating;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter(autoApply = false)
public class RatingConverter implements AttributeConverter<Rating, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Rating rating) {
        if (rating == null) return null;
        return new BigDecimal(rating.getDbValue());
    }

    @Override
    public Rating convertToEntityAttribute(BigDecimal value) {
        return Rating.from(value);
    }

}
