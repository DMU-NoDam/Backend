package NoDam.Demo.common.converter;

import NoDam.Demo.plan.dto.response.RouteInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RouteInfoConverter implements AttributeConverter<RouteInfo, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(RouteInfo routeInfo) {
        if (routeInfo == null) return null;
        try {
            return objectMapper.writeValueAsString(routeInfo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("RouteInfo 직렬화 실패", e);
        }
    }

    @Override
    public RouteInfo convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, RouteInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("RouteInfo 역직렬화 실패", e);
        }
    }
}
