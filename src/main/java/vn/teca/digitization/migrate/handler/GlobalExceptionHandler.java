package vn.teca.digitization.migrate.handler;

import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.UnknownOperatorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import vn.teca.digitization.migrate.config.MessageTemplate;
import vn.teca.digitization.migrate.exception.PartialUpdateException;
import vn.teca.digitization.migrate.exception.EntityValidationException;
import vn.teca.digitization.migrate.exception.ResourceNotFoundException;
import vn.teca.digitization.migrate.exception.RestTemplateException;
import vn.teca.digitization.migrate.model.ErrorDetail;
import vn.teca.digitization.migrate.util.MapperUtil;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private MessageTemplate messageTemplate;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), ex.getMessage(), "", request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<?> entityValidationException(EntityValidationException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), ex.getMessage(), ex.getDetails(), request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> validationException(ValidationException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), ex.getMessage(), "", request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({RSQLParserException.class, UnknownOperatorException.class})
    public ResponseEntity<?> queryParserException(Exception ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), messageTemplate.message("error.validation"), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PartialUpdateException.class)
    public ResponseEntity<?> partialUpdateException(PartialUpdateException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), messageTemplate.message("error.validation"), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalAccessException.class, InvocationTargetException.class})
    public ResponseEntity<?> illegalArgumentException(Exception ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), messageTemplate.message("error.validation"), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> authenticationException(AuthenticationException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), ex.getMessage(), "", request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> accessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), ex.getMessage(), "", request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<?> optimisticLockingFailureException(OptimisticLockingFailureException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(),
                messageTemplate.message("error.optimistic-locking"),
                "",
                request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error(ex.getMessage());
        ErrorDetail errorDetail = new ErrorDetail(new Date(), messageTemplate.message("error.system"), "", request.getDescription(false));
        Map<String, String> errors = new HashMap<>();
        Class clazz = ex.getBindingResult().getTarget().getClass();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = MapperUtil.convertToJsonName(clazz, ((FieldError) error).getField());
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        if (!CollectionUtils.isEmpty(errors)) {
            errorDetail.setMessage(messageTemplate.message("error.validation"));
            errorDetail.setDetail(errors);
        }

        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        ErrorDetail errorDetail = new ErrorDetail(new Date(), ex.getMessage(), "", request.getDescription(false));
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations()
                .parallelStream()
                .forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        if (!CollectionUtils.isEmpty(errors)) {
            errorDetail.setMessage(messageTemplate.message("error.validation"));
            errorDetail.setDetail(errors);
        }

        return new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RestTemplateException.class)
    public final ResponseEntity<?> handleRestTemplateException(RestTemplateException ex, WebRequest request) {
        log.error(ex.getMessage());
        ErrorDetail errorDetail = new ErrorDetail(new Date(), messageTemplate.message("error.system"), "", request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalExceptionHandler(Exception ex, WebRequest request) {
        log.error(ex.getMessage());
        ErrorDetail errorDetail = new ErrorDetail(new Date(), messageTemplate.message("error.system"), "", request.getDescription(false));
        return new ResponseEntity<>(errorDetail, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
