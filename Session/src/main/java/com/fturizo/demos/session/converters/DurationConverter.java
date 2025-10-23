package com.fturizo.demos.session.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

/**
 *
 * @author Fabio Turizo
 */
@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, String>{

    @Override
    public String convertToDatabaseColumn(Duration value) {
        return value.toString();
    }

    @Override
    public Duration convertToEntityAttribute(String text) {
        return Duration.parse(text);
    }
}
