package vn.teca.digitization.migrate.conveter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.Converter;

import java.util.Objects;

public class JsonConverter implements Converter {

    private ObjectMapper mapper;

    public JsonConverter() {
        super();
        mapper = new ObjectMapper();
    }

    @Override
    public <T> T convert(Class<T> type, Object value) {
        if (Objects.isNull(value)) {
            return null;
        } else {
            return mapper.convertValue(value, type);
        }
    }
}
