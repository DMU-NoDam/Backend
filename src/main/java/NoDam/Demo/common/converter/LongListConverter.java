package NoDam.Demo.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream().map(String::valueOf).reduce((a, b) -> a + DELIMITER + b).orElse("");
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        return Arrays.stream(dbData.split(DELIMITER)).map(Long::valueOf).toList();
    }
}
