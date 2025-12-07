package com.planitsquare.holiday.api;

import com.planitsquare.holiday.model.HolidayInfoDto;
import com.planitsquare.holiday.model.SearchFilterDto;
import com.planitsquare.holiday.model.request.PageInfoRequest;
import com.planitsquare.holiday.model.request.SearchFilterRequest;
import com.planitsquare.holiday.model.response.CommonResponse;
import com.planitsquare.holiday.service.HolidayManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;


@RestController
public class HolidayController {

    private final HolidayManageService service;

    public HolidayController(HolidayManageService service) {
        this.service = service;
    }

    @GetMapping("/api/holiday/data/init")
    @Operation(summary = "공휴일 데이터 초기화",
            description = "공휴일 데이터를 초기화합니다. 실행일의 년도와 해당 년도를 제외한 5년전 데이터를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "503", description = "외부 API 오류 혹은 기타 원인으로 인한 실패")
    })
    public CommonResponse<String> init(){
        service.initHolidayData();
        return new CommonResponse("SUCCESS");
    }

    @GetMapping("/api/holiday/search/year/{year}")
    @Operation(summary = "연도별 공휴일 데이터 검색",
            description = "연도별 공휴일 데이터를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "503", description = "외부 API 오류 혹은 기타 원인으로 인한 실패")
    })
    public Page<HolidayInfoDto> searchFromYear(@PathVariable int year
            , @ParameterObject SearchFilterRequest filter
            , @ParameterObject PageInfoRequest page
            , Errors e){

        return service.getHolidayInfoByYear(year, page, SearchFilterDto.of(filter));
    }

    @GetMapping("/api/holiday/search/country/{country}")
    @Operation(summary = "국가별 공휴일 데이터 검색",
            description = "국가별 공휴일 데이터 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
            , @ApiResponse(responseCode = "404", description = "해당 국가의 공휴일 데이터가 존재하지 않습니다.")
            , @ApiResponse(responseCode = "503", description = "외부 API 오류 혹은 기타 원인으로 인한 실패")
    })
    public Page<HolidayInfoDto> searchFromCountry(@PathVariable String country
            , @ParameterObject SearchFilterRequest filter
            , @ParameterObject PageInfoRequest page
            , Errors e){

        return service.getHolidayInfoByCountry(country, page, SearchFilterDto.of(filter));
    }

    @PutMapping("/api/holiday/refresh/{country}/{year}")
    @Operation(summary = "공휴일 데이터 갱신",
            description = "호출한 국가/연도의 공휴일 데이터를 갱신합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "503", description = "외부 API 오류 혹은 기타 원인으로 인한 실패")
    })
    public CommonResponse<String> refreshData(@PathVariable String country, @PathVariable int year){
        service.refreshHolidayData(year, country);
        return new CommonResponse("SUCCESS");
    }

    @DeleteMapping("/api/holiday/{country}/{year}")
    @Operation(summary = "공휴일 데이터 삭제",
            description = "호출한 국가/연도의 공휴일 데이터를 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "503", description = "외부 API 오류 혹은 기타 원인으로 인한 실패")
    })
    public CommonResponse<String> deleteData(@PathVariable String country, @PathVariable int year){
        service.deleteHolidayInfo(year, country);
        return new CommonResponse("SUCCESS");
    }

}