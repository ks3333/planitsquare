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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HolidayController {

    private final HolidayManageService service;

    public HolidayController(HolidayManageService service) {
        this.service = service;
    }

    @GetMapping("/api/holiday/data/init")
    @Operation(summary = "공휴일 데이터 초기화",
            description = "공휴일 데이터를 초기화합니다. 실행일의 년도와 해당 년도를 제외한 5년전 데이터를 생성합니다."            )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "503", description = "외부 API 오류 혹은 기타 원인으로 인한 실패")
    })
    public CommonResponse<String> init(){
        service.initHolidayData();
        return new CommonResponse("SUCCESS");
    }

    @GetMapping("/api/holiday/search/year/{year}")
    @Operation(summary = "공휴일 데이터 초기화",
            description = "공휴일 데이터를 초기화합니다. 실행일의 년도와 해당 년도를 제외한 5년전 데이터를 생성합니다."            )
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
    @Operation(summary = "공휴일 데이터 초기화",
            description = "공휴일 데이터를 초기화합니다. 실행일의 년도와 해당 년도를 제외한 5년전 데이터를 생성합니다."            )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "503", description = "외부 API 오류 혹은 기타 원인으로 인한 실패")
    })
    public Page<HolidayInfoDto> searchFromCountry(@PathVariable String country
            , @ParameterObject SearchFilterRequest filter
            , @ParameterObject PageInfoRequest page
            , Errors e){

        return service.getHolidayInfoByCountry(country, page, SearchFilterDto.of(filter));
    }
}
