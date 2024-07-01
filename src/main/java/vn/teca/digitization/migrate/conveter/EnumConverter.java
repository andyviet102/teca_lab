package vn.teca.digitization.migrate.conveter;

import org.apache.commons.beanutils.Converter;

import java.util.Objects;

public class EnumConverter implements Converter {

    @Override
    public <T> T convert(Class<T> type, Object value) {
        if (Objects.isNull(value)) {
            return null;
        } else {
            return (T) value;
        }
    }
}
