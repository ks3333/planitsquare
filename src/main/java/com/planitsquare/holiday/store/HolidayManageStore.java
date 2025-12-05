package com.planitsquare.holiday.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.commons.lang.Assert;
import com.planitsquare.holiday.entity.HolidayInfoEntity;
import com.planitsquare.holiday.model.CountryInfoDto;
import com.planitsquare.holiday.model.HolidayCountryDto;
import com.planitsquare.holiday.model.HolidayInfoDto;
import com.planitsquare.holiday.model.HolidayTypeDto;
import com.planitsquare.holiday.repository.HolidayInfoJdbcRepository;
import com.planitsquare.holiday.repository.CountryInfoEntityRepository;
import com.planitsquare.holiday.repository.HolidayCountryEntityRepository;
import com.planitsquare.holiday.repository.HolidayInfoEntityRepository;
import com.planitsquare.holiday.repository.HolidayTypesEntityRepository;
import com.planitsquare.holiday.repository.HolidayInfoQueryDslRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class HolidayManageStore {


    private final HolidayInfoJdbcRepository holidayInfoJdbcRepository;

    private final HolidayCountryEntityRepository holidayCountryEntityRepository;

    private final HolidayInfoEntityRepository holidayInfoEntityRepository;

    private final HolidayTypesEntityRepository holidayTypesEntityRepository;

    private final CountryInfoEntityRepository countryInfoEntityRepository;

    private final HolidayInfoQueryDslRepository holidayInfoQueryDslRepository;

    public HolidayManageStore(HolidayInfoJdbcRepository holidayInfoJdbcRepository, HolidayCountryEntityRepository holidayCountryEntityRepository, HolidayInfoEntityRepository holidayInfoEntityRepository, HolidayTypesEntityRepository holidayTypesEntityRepository, CountryInfoEntityRepository countryInfoEntityRepository, HolidayInfoQueryDslRepository holidayInfoQueryDslRepository) {
        this.holidayInfoJdbcRepository = holidayInfoJdbcRepository;
        this.holidayCountryEntityRepository = holidayCountryEntityRepository;
        this.holidayInfoEntityRepository = holidayInfoEntityRepository;
        this.holidayTypesEntityRepository = holidayTypesEntityRepository;
        this.countryInfoEntityRepository = countryInfoEntityRepository;
        this.holidayInfoQueryDslRepository = holidayInfoQueryDslRepository;
    }


    public void holidayInfoBatchInsert(List<HolidayInfoDto> holidays) {
        Assert.notEmpty(holidays, "Holidays list must not be empty");
        holidayInfoJdbcRepository.holidayInfoBatchInsert(holidays);
    }

    public void countryInfoBatchInsert(List<CountryInfoDto> countries) {
        Assert.notEmpty(countries, "Country list must not be empty");
        holidayInfoJdbcRepository.countryInfoBatchInsert(countries);
    }

    public void holidayCountryBatchInsert(List<HolidayCountryDto> countryDtoList) {
        Assert.notEmpty(countryDtoList, "Holiday country list must not be empty");
        countryDtoList.stream().filter(f -> f.getCountry() == null || f.getHolidayInfoSeq() == null || f.getHolidayInfoSeq() == 0L).findAny().ifPresent(f -> {
            throw new IllegalArgumentException("HolidayCountryDto contains null values: %s".formatted(f.toString()));
        });
        holidayInfoJdbcRepository.holidayCountryBatchInsert(countryDtoList);
    }

    public void holidayTypeBatchInsert(List<HolidayTypeDto> holidayTypeDtoList) {
        Assert.notEmpty(holidayTypeDtoList, "Holiday type list must not be empty");
        holidayTypeDtoList.stream().filter(f -> f.getType() == null || f.getHolidayInfoSeq() == null || f.getHolidayInfoSeq() == 0L).findAny().ifPresent(f -> {
            throw new IllegalArgumentException("holidayTypeDtoList contains null values: %s".formatted(f.toString()));
        });
        holidayInfoJdbcRepository.holidayTypeBatchInsert(holidayTypeDtoList);
    }

    public List<HolidayInfoDto> getHolidayInfoAll(){
        return holidayInfoEntityRepository.findAll().stream().map(HolidayInfoEntity::makeDto).toList();
    }
}
