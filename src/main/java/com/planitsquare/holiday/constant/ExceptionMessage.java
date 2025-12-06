package com.planitsquare.holiday.constant;

import lombok.Getter;

@Getter
public enum ExceptionMessage {

    UNKNOWN_ERROR(9999, "알수없는 오류입니다. 잠시 후 다시 시도 해 주십시오."),
    DATA_NOT_FOUND(1, "데이터를 찾을 수 없습니다."),
    INVALID_PARAMETER(2, "잘못된 파라미터입니다"),
    EXTERNAL_API_CALL_ERROR(3, "외부 API 호출에 일시작 오류가 있습니다. 잠시 후 다시 시도 해 주십시오");

    private final int errorCode;
    private final String errorMsg;

    ExceptionMessage(int errorCode, String errorMsg){
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
}
