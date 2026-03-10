package com.storix.domain.domains.works.converter;

import com.storix.domain.domains.works.domain.WorksType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class WorksTypeConverter implements AttributeConverter<WorksType, String> {

    @Override
    public String convertToDatabaseColumn(WorksType attribute) {

        if  (attribute == null) { return null; }

        return attribute.getDbValue();
    }

    @Override
    public WorksType convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isEmpty()) { return null; }

        return Stream.of(WorksType.values())
                .filter(c -> c.getDbValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown DB value: " + dbData));
    }
}
