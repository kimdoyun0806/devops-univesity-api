package com.beyond.university.common.exception.handler;

import com.beyond.university.common.exception.UniversityException;
import com.beyond.university.common.exception.dto.ApiErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UniversityException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(UniversityException e) {

        log.error("UniversityException : {}", e.getMessage());

        return new ResponseEntity<>(
                new ApiErrorResponseDto(e.getStatus().value(), e.getType(), e.getMessage()),
                e.getStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(MethodArgumentNotValidException e) {
        StringBuilder errors = new StringBuilder();

        log.error("MethodArgumentNotValidException : {}", e.getMessage());

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors
                    .append(fieldError.getField())
                    .append("(")
                    .append(fieldError.getDefaultMessage())
                    .append("), ");
        }

        errors.replace(errors.lastIndexOf(","), errors.length(), "");

        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        errors.toString()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleException(Exception e) {

        log.error("Global Exception : {}", e.getMessage());

        return new ResponseEntity<>(
                new ApiErrorResponseDto(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.name(),
                        e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
