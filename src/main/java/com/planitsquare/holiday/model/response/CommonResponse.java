package com.planitsquare.holiday.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonResponse<T> {
    T result;

    public CommonResponse(T result) {
        this.result = result;
    }
}
