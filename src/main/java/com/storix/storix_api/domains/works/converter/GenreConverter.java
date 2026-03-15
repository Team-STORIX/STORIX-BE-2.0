package com.storix.storix_api.domains.works.converter;

import com.storix.storix_api.domains.works.domain.Genre;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class GenreConverter implements AttributeConverter<Genre, String> {


    @Override
    public String convertToDatabaseColumn(Genre attribute) {

        if  (attribute == null) { return null; }

        return attribute.getDbValue();
    }

    @Override
    public Genre convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.isEmpty()) { return null; }

        return Stream.of(Genre.values())
                .filter(c -> c.getDbValue().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown DB value: " + dbData));
    }
}
