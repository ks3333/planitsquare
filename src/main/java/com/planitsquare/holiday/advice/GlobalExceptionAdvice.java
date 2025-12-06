package com.planitsquare.holiday.advice;

import com.planitsquare.holiday.Exception.DateNotExistException;
import com.planitsquare.holiday.Exception.RestClientCallException;
import com.planitsquare.holiday.api.HolidayController;
import com.planitsquare.holiday.model.response.ErrorResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.planitsquare.holiday.constant.ExceptionMessage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ParameterValidErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ParameterValidErrorResponse errorResponse = new ParameterValidErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "입력값 검증 실패",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ParameterValidErrorResponse> handleMethodValidationExceptions(
            HandlerMethodValidationException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getAllValidationResults().forEach(result -> {
            result.getResolvableErrors().forEach(error -> {
                errors.put(result.getMethodParameter().getParameterName(),
                        error.getDefaultMessage());
            });
        });

        ParameterValidErrorResponse errorResponse = new ParameterValidErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "입력값 검증 실패",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RestClientCallException.class)
    public ResponseEntity<ErrorResponse> handleRestClientCallException(
            RestClientCallException e) {

        ErrorResponse errorResponse = new ErrorResponse(
                ExceptionMessage.EXTERNAL_API_CALL_ERROR.getErrorCode(),
                ExceptionMessage.EXTERNAL_API_CALL_ERROR.getErrorMsg()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }


    @ExceptionHandler(DateNotExistException.class)
    public ResponseEntity<ErrorResponse> handleDateNotExistException(
            DateNotExistException e) {

        ErrorResponse errorResponse = new ErrorResponse(
                ExceptionMessage.DATA_NOT_FOUND.getErrorCode(),
                ObjectUtils.isEmpty(e.getMessage()) ? ExceptionMessage.DATA_NOT_FOUND.getErrorMsg() : e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleDefaultException(
            Exception e) {

        log.info("Unhandled exception occurred: {} ", e.getMessage());
        log.error("Exception details: ", e);

        ErrorResponse errorResponse = new ErrorResponse(
                ExceptionMessage.UNKNOWN_ERROR.getErrorCode(),
                ExceptionMessage.UNKNOWN_ERROR.getErrorMsg()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @Getter
    @RequiredArgsConstructor
    public static class ParameterValidErrorResponse {
        private final int status;
        private final String message;
        private final Map<String, String> errors;
        private final LocalDateTime timestamp;
    }
}
