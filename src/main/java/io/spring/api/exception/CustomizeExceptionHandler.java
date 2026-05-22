package io.spring.api.exception;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class CustomizeExceptionHandler {

  @ExceptionHandler(InvalidAuthenticationException.class)
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ResponseBody
  public Map<String, Object> handleInvalidAuthentication(InvalidAuthenticationException e) {
    return Map.of("message", e.getMessage());
  }

  @ExceptionHandler(WebExchangeBindException.class)
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResource handleWebExchangeBindException(WebExchangeBindException e) {
    List<FieldErrorResource> errorResources =
        e.getFieldErrors().stream()
            .map(
                fieldError ->
                    new FieldErrorResource(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getCode(),
                        fieldError.getDefaultMessage()))
            .collect(Collectors.toList());
    return new ErrorResource(errorResources);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResource handleConstraintViolation(ConstraintViolationException ex) {
    List<FieldErrorResource> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      FieldErrorResource fieldErrorResource =
          new FieldErrorResource(
              violation.getRootBeanClass().getName(),
              getParam(violation.getPropertyPath().toString()),
              violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
              violation.getMessage());
      errors.add(fieldErrorResource);
    }
    return new ErrorResource(errors);
  }

  private String getParam(String s) {
    String[] splits = s.split("\\.");
    if (splits.length == 1) {
      return s;
    } else {
      return String.join(".", Arrays.copyOfRange(splits, 2, splits.length));
    }
  }
}
