package com.storix.domain.domains.works.converter;

import com.storix.domain.domains.works.domain.Platform;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class PlatformConverter implements AttributeConverter<Platform, String>{

    @Override
    public String convertToDatabaseColumn(Platform attribute) {

        if  (attribute == null) { return null; }

        return attribute.getDbValue();
    }

    @Override
    public Platform convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isEmpty()) { return null; }

        return Stream.of(Platform.values())
                .filter(c -> c.getDbValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown DB value: " + dbData));
    }
}
