package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(EmailValidateException e) {
        log.error("Email validation error" + "\n" + e.getMessage());
        return new ErrorResponse("Email validation error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleStorageException(StorageException e) {
        log.error("Storage error - object not found" + "\n" + e.getMessage());
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }
}