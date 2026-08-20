package NoDam.Demo.plan.behaviour.converter;

import NoDam.Demo.plan.behaviour.domain.Behaviour;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BehaviourConverter implements AttributeConverter<Behaviour, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Behaviour behaviour) {
        if (behaviour == null) return null;
        try {
            return objectMapper.writeValueAsString(behaviour);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Behaviour 직렬화 실패", e);
        }
    }

    @Override
    public Behaviour convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Behaviour.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Behaviour 역직렬화 실패", e);
        }
    }
}
