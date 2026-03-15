package com.storix.storix_api.domains.works.converter;

import com.storix.storix_api.domains.works.domain.AgeClassification;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class AgeClassificationConverter implements AttributeConverter<AgeClassification, String> {

    @Override
    public String convertToDatabaseColumn(AgeClassification attribute) {

        if (attribute == null) { return null; }

        return attribute.getDbValue();
    }

    @Override
    public AgeClassification convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isEmpty()) { return null; }

        return Stream.of(AgeClassification.values())
                .filter(c -> c.getDbValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown DB value: " + dbData));
    }

}
