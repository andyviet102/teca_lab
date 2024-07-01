package vn.teca.digitization.migrate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.teca.digitization.migrate.config.MessageTemplate;
import vn.teca.digitization.migrate.exception.EntityValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ValidationService<T> {

    @Autowired
    private Validator validator;

    @Autowired
    private MessageTemplate messageTemplate;

    public void validate(@NotNull T entity) throws EntityValidationException {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        Map<String, String> errors = new HashMap<>();

        violations.forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        if (!errors.isEmpty()) {
            throw new EntityValidationException(messageTemplate.message("error.validation"), errors);
        }
    }
}
