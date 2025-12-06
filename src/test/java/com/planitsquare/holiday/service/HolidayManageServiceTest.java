package com.planitsquare.holiday.service;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class HolidayManageServiceTest {

    @Autowired
    HolidayManageService service;

    @Test
    public void initHolidayDataTest(){

        service.initHolidayData();

    }

}
