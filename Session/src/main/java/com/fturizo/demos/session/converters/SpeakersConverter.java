package com.fturizo.demos.session.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author fabio
 */
@Converter
public class SpeakersConverter implements AttributeConverter<List<String>, String>{

    private static final String SEPARATOR = ";";

    @Override
    public String convertToDatabaseColumn(List<String> names) {
       return String.join(SEPARATOR, names);
    }

    @Override
    public List<String> convertToEntityAttribute(String text) {
        return Arrays.stream(text.split(SEPARATOR)).collect(Collectors.toList());
    }
}
