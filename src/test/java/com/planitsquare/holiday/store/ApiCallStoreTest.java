package com.planitsquare.holiday.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.planitsquare.holiday.model.CountryInfoDto;
import com.planitsquare.holiday.model.HolidayInfoDto;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class ApiCallStoreTest {

    @Autowired
    ApiCallStore store;

    @Autowired
    static ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setUpClass() throws IOException {
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void getCountryCodeListTest() throws JsonProcessingException {
        List<CountryInfoDto> result = store.getCountryCodeList();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    @Test
    public void getHolidayListTest() throws JsonProcessingException {
        List<HolidayInfoDto> result = store.getHolidayList(2025, "KR");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

    }
}
