package com.planitsquare.holiday.api;

import com.planitsquare.holiday.model.response.CommonResponse;
import com.planitsquare.holiday.service.HolidayManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HolidayController {

    private final HolidayManageService service;

    public HolidayController(HolidayManageService service) {
        this.service = service;
    }

    @GetMapping("/api/holiday/data/init")
    @Operation(summary = "테스트",
            description = "테스트."            )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공")
    })
    public CommonResponse<String> init(){
        service.initHolidayData();
        return new CommonResponse("SUCCESS");
    }
}
