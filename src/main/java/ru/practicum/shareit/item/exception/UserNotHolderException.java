package ru.practicum.shareit.item.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotHolderException extends RuntimeException {
    public UserNotHolderException(String message){
        super(message);
    }
}
