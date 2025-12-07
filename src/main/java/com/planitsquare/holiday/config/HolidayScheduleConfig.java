package com.planitsquare.holiday.config;

import com.planitsquare.holiday.service.HolidayManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Configuration
@EnableScheduling
@Slf4j
public class HolidayScheduleConfig {

    private final HolidayManageService service;

    private final int SYNC_YEAR_INTERVAL = 2;

    public HolidayScheduleConfig(HolidayManageService service) {

        this.service = service;
    }

    @Scheduled(cron = "0 0 1 2 1 *")
    public void runEveryFiveMinutes() {
        log.info("start holiday data synchronization schedule task");
        LocalDate now = LocalDate.now();

        try{
            IntStream.rangeClosed(0, SYNC_YEAR_INTERVAL - 1)
                    .mapToObj(now::minusYears)
                    .map(LocalDate::getYear)
                    .forEach(service::scheduleSynchronizeHolidayData);
        } catch (Exception e){
            log.error("Exception during holiday data synchronization schedule task", e.getMessage());
            log.error("ERROR DETAIL : ", e);
        }

        log.info("end holiday data synchronization schedule task");
    }
}
