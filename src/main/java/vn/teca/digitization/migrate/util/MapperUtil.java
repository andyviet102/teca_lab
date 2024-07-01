package vn.teca.digitization.migrate.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.stream.Stream;

@Service
@Slf4j
public class MapperUtil {

    public static String convertToJsonName(Class<?> clazz, String fieldName) {
        return Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.getName().equals(fieldName))
                .map(Field::getDeclaredAnnotations)
                .flatMap(Stream::of)
                .filter(annotation -> annotation instanceof JsonProperty)
                .map(annotation -> ((JsonProperty) annotation).value())
                .filter(StringUtils::isNoneBlank)
                .findFirst().orElse(fieldName);
    }
}
