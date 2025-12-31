package com.toty.user.domain.vo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Email Value Object를 DB String으로 자동 변환하는 JPA Converter
 */
@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        return email == null ? null : email.getValue();
    }

    @Override
    public Email convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Email.of(dbData);
    }
}
