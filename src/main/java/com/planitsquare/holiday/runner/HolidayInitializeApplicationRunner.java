package com.planitsquare.holiday.runner;

import com.planitsquare.holiday.service.HolidayManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HolidayInitializeApplicationRunner implements ApplicationRunner {

    private final HolidayManageService holidayManageService;

    public HolidayInitializeApplicationRunner(HolidayManageService holidayManageService) {
        this.holidayManageService = holidayManageService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting holiday data initialization...");
        holidayManageService.initHolidayData();
        log.info("holiday data initialization done");
    }
}
