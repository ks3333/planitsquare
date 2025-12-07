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
        LocalDate now = LocalDate.now();

        IntStream.rangeClosed(0, SYNC_YEAR_INTERVAL - 1)
                .mapToObj(now::minusYears)
                .map(LocalDate::getYear)
                .forEach(service::scheduleSynchronizeHolidayData);
    }
}
