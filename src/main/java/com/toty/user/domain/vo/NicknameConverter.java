package com.toty.user.domain.vo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Nickname Value Object를 DB String으로 자동 변환하는 JPA Converter
 */
@Converter(autoApply = true)
public class NicknameConverter implements AttributeConverter<Nickname, String> {

    @Override
    public String convertToDatabaseColumn(Nickname nickname) {
        return nickname == null ? null : nickname.getValue();
    }

    @Override
    public Nickname convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Nickname.of(dbData);
    }
}
