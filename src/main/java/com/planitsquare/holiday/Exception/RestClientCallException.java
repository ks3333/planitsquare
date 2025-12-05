package com.planitsquare.holiday.Exception;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestClientCallException extends RuntimeException {

    final String message;

    final int code;

    final String responseResultMessage;

    public RestClientCallException(String message, int code, String responseResultMessage) {
        super(message);
        this.message = message;
        this.code = code;
        this.responseResultMessage = responseResultMessage;
    }

}
