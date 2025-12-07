package com.planitsquare.holiday.model.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "페이지 정보 요청 객체")
public class PageInfoRequest {
    @Parameter(
            name = "page",
            description = "페이지 번호",
            example = "1"
    )
    @Schema(description = "페이지 번호")
    int page;

    @Parameter(
            name = "size",
            description = "페이지 사이즈",
            example = "10"
    )
    @Schema(description = "페이지 사이즈")
    int size;

    @Parameter(
            name = "sortTarget",
            description = "정렬 대상"
    )
    @Schema(description = "정렬 대상", allowableValues = {"NAME", "DATE", "COUNTRYCODE", "LOCALNAME"})
    String sortTarget;

    @Parameter(
            name = "toDate",
            description = "정렬 방식"
    )
    @Schema(description = "정렬 대상", allowableValues = {"ASC", "DESC"})
    String sort;
}
