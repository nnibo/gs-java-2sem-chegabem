package com.nicolas.chegabem.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Converter
public class HoraVarcharConverter implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalTime hora) {
        return hora == null ? null : hora.format(FORMATO);
    }

    @Override
    public LocalTime convertToEntityAttribute(String valor) {
        return (valor == null || valor.isBlank()) ? null : LocalTime.parse(valor.trim(), FORMATO);
    }
}
