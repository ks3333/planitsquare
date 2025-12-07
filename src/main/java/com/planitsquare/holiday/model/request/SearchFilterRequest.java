package com.planitsquare.holiday.model.request;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.Schema;
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
            name = "fromDate",
            description = "시작일"
    )
    @Schema(description = "검색 시작일(YYYY-MM-DD)")
    @JsonView({SearchFilterRequestView.yearSearch.class, SearchFilterRequestView.countrySearch.class})
    LocalDate fromDate;

    @Parameter(
            name = "toDate",
            description = "종료일"
    )
    @Schema(description = "검색 종료일(YYYY-MM-DD)")
    @JsonView({SearchFilterRequestView.yearSearch.class, SearchFilterRequestView.countrySearch.class})
    LocalDate toDate;

    @Parameter(
            name = "counties",
            description = "시/도 코드 목록 : EX - US-CA, US-NY",
            explode = Explode.FALSE
    )
    @Schema(description = "시/도 코드 목록")
    @JsonView({SearchFilterRequestView.yearSearch.class, SearchFilterRequestView.countrySearch.class})
    List<String> counties;

    @Parameter(
            name = "types",
            description = "공휴일 종류 목록 : EX - Public, Bank, School, Authorities",
            explode = Explode.FALSE
    )
    @Schema(description = "공휴일 종류 목록")
    @JsonView({SearchFilterRequestView.yearSearch.class, SearchFilterRequestView.countrySearch.class})
    List<String> types;

    @Parameter(
            name = "countryCode",
            description = "국가 코드"
    )
    @Schema(description = "국가 코드", example = "KR")
    @JsonView(SearchFilterRequestView.yearSearch.class)
    String countryCode;

    @Parameter(
            name = "year",
            description = "연도"
    )
    @Schema(description = "연도", example = "2025")
    @JsonView(SearchFilterRequestView.countrySearch.class)
    int year;
}
