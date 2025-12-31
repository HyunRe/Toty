package com.toty.user.domain.vo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * PhoneNumber Value Object를 DB String으로 자동 변환하는 JPA Converter
 */
@Converter(autoApply = true)
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {

    @Override
    public String convertToDatabaseColumn(PhoneNumber phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.getValue();
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PhoneNumber.of(dbData);
    }
}
