package com.planitsquare.holiday.model.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SearchFilterRequest {

    @Parameter(
            name = "counties",
            description = "시/도 코드 목록",
            explode = Explode.FALSE
    )
    List<String> counties;

    @Parameter(
            name = "fromDate",
            description = "시작일"
    )
    LocalDate fromDate;

    @Parameter(
            name = "toDate",
            description = "종료일"
    )
    LocalDate toDate;

    @Parameter(
            name = "types",
            description = "공휴일 종류 목록",
            explode = Explode.FALSE
    )
    List<String> types;
}
