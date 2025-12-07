package com.planitsquare.holiday.model.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageInfoRequest {
    @Parameter(
            name = "page",
            description = "페이지 번호",
            example = "1",
            explode = Explode.FALSE
    )
    int page;

    @Parameter(
            name = "size",
            description = "페이지 사이즈",
            example = "1"
    )
    int size;

    @Parameter(
            name = "sortTarget",
            description = "정렬 대상",
            example = "YEAR"
    )
    String sortTarget;

    @Parameter(
            name = "toDate",
            description = "정렬 방식",
            example = "ASC"
    )
    String sort;
}
