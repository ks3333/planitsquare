package com.planitsquare.holiday.Exception;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DateNotExistException extends RuntimeException {

    final String message;

    public DateNotExistException(String message) {
        super(message);
        this.message = message;
    }

}
