package com.planitsquare.holiday.model;

import com.planitsquare.holiday.model.request.SearchFilterRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchFilterDto {

    @Setter
    String country;

    @Setter
    Integer year;

    List<String> counties;

    LocalDate fromDate;

    LocalDate toDate;

    List<String> types;

    public static SearchFilterDto of(SearchFilterRequest request){
        SearchFilterDto dto = new SearchFilterDto();
        dto.counties = request.getCounties();
        dto.fromDate = request.getFromDate();
        dto.toDate = request.getToDate();
        dto.types = request.getTypes();
        return dto;

    }
}
