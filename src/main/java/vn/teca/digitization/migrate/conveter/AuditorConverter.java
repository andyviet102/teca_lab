package vn.teca.digitization.migrate.conveter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import vn.teca.digitization.migrate.model.Auditor;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
@Slf4j
public class AuditorConverter implements AttributeConverter<Auditor, String> {
    @Override
    public String convertToDatabaseColumn(Auditor auditor) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(auditor);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public Auditor convertToEntityAttribute(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(s, Auditor.class);
            } catch (Exception ex) {
                log.error(ex.getMessage());
                return null;
            }
        }
    }
}
